package evgenykuzin.core.marketplace_api_integrations.ozon;

import com.github.evgenykuzin.core.db.dao.OzonProductDAO;
import com.github.evgenykuzin.core.db.dao.StockDAO;
import com.github.evgenykuzin.core.entities.Stock;
import com.github.evgenykuzin.core.entities.product.OzonProduct;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil;
import com.github.evgenykuzin.core.parser.SUPPLIER_NAME;
import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.Header;
import com.github.evgenykuzin.core.util.http.headers.HeadersModel;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.util.http.services.HttpService;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.PropertiesManager;
import com.google.gson.*;

import java.util.*;

import static com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManagerUtil.*;
import static com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManagerUtil.getSku;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.*;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getDoubleProp;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getIntProp;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getJsonProp;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getLongProp;
import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getStrProp;

public class OzonManager implements MPManager<Product>, Loggable {
    private static final Properties ozonProps = PropertiesManager.getProperties("ozon");
    private static final String OZON_CLIENT_ID = ozonProps.getProperty("client-id");
    private static final String OZON_API_KEY = ozonProps.getProperty("api-key");
    private static final String OZON_API_HOST = "http://api-seller.ozon.ru";
    public static final String WAREHOUSE_BAZA = "20658102477000";
    public static final String WAREHOUSE_KUDROVO = "21304218012000";

    private final ClosableHttpService httpService;

    private OzonManager() {
        this.httpService = new ClosableHttpService();
    }

    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        HeadersModel headers = new HeadersModelImpl(
                new Header("Client-Id", OZON_CLIENT_ID),
                new Header("Api-Key", OZON_API_KEY),
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7")
        );
        var req = httpService.constructRequest(
                OZON_API_HOST + mapping,
                httpMethod,
                headers,
                bodyBuilder
        );
        return new Gson()
                .fromJson(httpService
                        .getResponse(req)
                        .getResponseString(), JsonObject.class);
    }

    public JsonObject executePostRequest(String mapping, JsonBuilder builder) {
        return executeRequest(mapping, HttpService.POST, builder.build());
    }

    public JsonObject executePostRequest(String mapping) {
        return executeRequest(mapping, HttpService.POST, new JsonBuilder().build());
    }

    @Override
    public JsonObject updateProductStocks(Collection<Product> ozonProducts, String supplierName) {
        return executeWithMaxItems(ozonProducts, 100, productsForRequest -> {
            var res1 = updateProductStocksInWarehouse(productsForRequest, supplierName, WAREHOUSE_BAZA, 0);//База
            var res2 = updateProductStocksInWarehouse(productsForRequest, supplierName, WAREHOUSE_KUDROVO);//Кудрово
            return new JsonBuilder()
                    .add("baza", res1)
                    .add("kudrovo", res2)
                    .build()
                    .getJsonObject();
        });
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        var resultCollection = new ArrayList<JsonObject>();
        JsonArray productsListJson = new JsonArray();
        int page = 1;
        do {
            try {
                var pageFilter = new JsonBuilder()
                        .addProperty("page", String.valueOf(page))
                        .addProperty("page_size", "100");
                var req = executePostRequest("/v1/product/list", pageFilter);
                System.out.println("req = " + req);
                productsListJson = req.getAsJsonObject("result")
                        .getAsJsonArray("items");
                JsonBuilder infoJsonBuilder = new JsonBuilder()
                        .addNewArr("product_id");
                for (JsonElement element : productsListJson) {
                    var productId = element.getAsJsonObject()
                            .get("product_id")
                            .getAsString();
                    infoJsonBuilder.addPropertyInArr(productId, "product_id");
                }
                var infoResult = executePostRequest("/v2/product/info/list", infoJsonBuilder);
                var resultJson = infoResult.get("result");
                if (resultJson != null) {
                    var infoJsonArray = infoResult.get("result")
                            .getAsJsonObject()
                            .getAsJsonArray("items");
                    for (JsonElement element : infoJsonArray) {
                        resultCollection.add(element.getAsJsonObject());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        } while (productsListJson.size() > 0);
        return resultCollection;
    }

    @Override
    public Product constructProduct(JsonObject jsonObject) {
        String id = getStrProp(jsonObject, "id");
        String article = getStrProp(jsonObject, "offer_id");
        String barcode = getStrProp(jsonObject, "barcode");
        String name = getStrProp(jsonObject, "name");
        Double price = getDoubleProp(jsonObject, "price");
        JsonObject stocksObj = getJsonProp(jsonObject, "stocks");
        Integer stock = 0;
        if (stocksObj != null) {
            stock = getIntProp(stocksObj, "present");
        }
        if (stock == null) stock = 0;
        String skuFbs = getSku(jsonObject, "fbs");
        String skuFbo = getSku(jsonObject, "fbo");
        Long categoryId = getLongProp(jsonObject, "category_id");
        var urls = new ArrayList<String>();
        var urlsJson = jsonObject.getAsJsonArray("images");
        for (var url : urlsJson) {
            urls.add(url.getAsString());
        }
        String brand = null;
        Double concPrice = null;
        String concProdUrl = null;
        String searchBarcode = null;
        SUPPLIER_NAME supplier = null;
        Product product = new Product();
        OzonProduct ozonProduct = null;
        try {
            ozonProduct = OzonProductDAO.getInstance().get(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ozonProduct == null) {
            ozonProduct = new OzonProduct();
            ozonProduct.setOzonId(id);
        }
        ozonProduct.setSkuFbs(skuFbs);
        ozonProduct.setSkuFbo(skuFbo);
        ozonProduct.setCategoryId(categoryId);
        product.setPrice(price);
        //ozonProduct.setConcurrentPrice(concPrice);
        //ozonProduct.setConcurrentProductUrl(concProdUrl);
        product.setName(name);
        product.setBrandName(brand);
        product.setArticle(article);
        product.setBarcode(barcode);
        product.setStock(Stock.tempStock(stock));
        product.setSupplierName(supplier);
        product.setUrls(urls);
        product.setOzonProduct(ozonProduct);
        return product;
    }

    @Override
    public JsonObject constructJsonFromProduct(Product product) {
        return null;
    }

    @Override
    public List<Product> getOrderedProducts() {
        var result = new ArrayList<Product>();
        var resp = executePostRequest("/v2/posting/fbs/list", new JsonBuilder("{\n" +
                "  \"dir\": \"asc\",\n" +
                "  \"filter\": {\n" +
                "    \"status\": \"awaiting_packaging\"\n" +
                "  },\n" +
                "  \"limit\": 50,\n" +
                "  \"offset\": 0,\n" +
                "  \"with\": {\n" +
                "    \"barcodes\": true\n" +
                "  }\n" +
                "}"));
        JsonArray productsJson;
        JsonArray resultArray = resp.getAsJsonArray("result");
        if (resultArray == null || resultArray.size() <= 0) return new ArrayList<>();
        for (var order : resultArray) {
            productsJson = order
                    .getAsJsonObject()
                    .getAsJsonArray("products");
            for (var element : productsJson) {
                var jsonProduct = element.getAsJsonObject();
                var sku = jsonProduct.get("sku").getAsString();
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(jsonProduct.get("quantity").getAsString());
                } catch (NumberFormatException ignored) { }
                var product = getProductFromOzonBy("sku", sku);
                for (int i = 0; i < quantity; i++) {
                    result.add(product);
                }
            }
        }
        StockDAO stockDAO = StockDAO.getInstance();
        for (var product : result) {
            Stock stock = Stock.getStock(product.getId());
            stock.addNegative(1);
            product.setStock(stock);
            stockDAO.update(stock);
        }
        return result;
    }

    @Override
    public JsonObject importProductsToMP(Collection<Product> products) {
        var jsn = new JsonBuilder(new JsonParser().parse("{\n" +
                "  \"filter\": {\n" +
                "    \"product_id\": [\n" +
                "      53571758\n" +
                "    ]\n" +
                "  }\n" +
                "}").getAsJsonObject());
        var req = executePostRequest("/v2/products/info/attributes", jsn);
        System.out.println("req = " + req);
        return req;
    }

    @Override
    public MP_NAME getName() {
        return MP_NAME.OZON;
    }

    public JsonObject updateProductStocksInWarehouse(Collection<Product> products, String supplierName, String warehouseId, Integer defaultStock) {
        var stocksInfoAdditionMap = new JsonObject();
        var jsonBuilder = new JsonBuilder()
                .addNewArr("stocks");
        for (var product : products) {
            if (product == null) continue;
            OzonProduct ozonProduct = product.getOzonProduct();
            if (ozonProduct == null) continue;
            Integer stock;
            if (defaultStock == null) {
                stock = product.getStock().computeStock();
            } else {
                stock = defaultStock;
            }
            String productIdStr = String.valueOf(ozonProduct.getOzonId());
            String stockStr = String.valueOf(stock);
            var stockObjectBuilder = new MPUtil.JsonBuilder()
                    .addProperty("product_id", productIdStr)
                    .addProperty("stock", stockStr)
                    .addProperty("warehouse_id", warehouseId);
            jsonBuilder.addInArr(stockObjectBuilder, "stocks");
            stocksInfoAdditionMap.addProperty(productIdStr, stockStr);
        }
        var resultJson = executePostRequest("/v2/products/stocks", jsonBuilder);
        var resultArray = resultJson.getAsJsonArray("result");
        if (resultArray != null) {
            for (JsonElement element : resultArray) {
                var errors = element.getAsJsonObject().getAsJsonArray("errors");
                if (errors != null && errors.size() > 0) {
                    var errorMsg = errors.get(0)
                            .getAsJsonObject()
                            .get("code")
                            .getAsString();
                    if (!errorMsg.equals("SKU_STOCK_NOT_CHANGE")
                            && !errorMsg.equals("NOT_FOUND")) {
                        logf("for %s: error update = %s", supplierName, element);
                    }
                } else {
                    var updatedStock = stocksInfoAdditionMap.get(element.getAsJsonObject().get("product_id").getAsString());
                    logf("for %s: success update = %s; with updated stock = %s", supplierName, element.toString(), updatedStock);
                }
            }
        } else {
            log(resultJson.toString());
        }
        return resultJson;
    }

    public JsonObject updateProductStocksInWarehouse(Collection<Product> ozonProducts, String supplierName, String warehouseId) {
        return updateProductStocksInWarehouse(ozonProducts, supplierName, warehouseId, null);
    }

    public Product getProductFromOzonBy(String by, String keyValue) {
        var resp = executePostRequest("/v2/product/info", new JsonBuilder().addProperty(by, keyValue));
        return constructProduct(resp.getAsJsonObject("result"));
    }

    public JsonObject updatePrices(Collection<? extends Product> products) {
        return executeWithMaxItems(products, 500, productsForRequest -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.addNewArr("prices");
            productsForRequest.forEach(product -> {
                OzonProduct ozonProduct = product.getOzonProduct();
                if (ozonProduct != null) {
                    double price = product.getPrice();
                    System.out.println("------------------------");
                    System.out.println("product = " + product);
                    System.out.println("price = " + price);
                    double oldPrice = price + ((price / 100) * (25 + new Random().nextInt(25)));
                    double premiumPrice = price - ((price / 100) * 7);
                    JsonBuilder priceBody = new JsonBuilder()
                            .addProperty("product_id", String.valueOf(ozonProduct.getOzonId()))
                            .addProperty("price", String.valueOf(price))
                            .addProperty("old_price", String.valueOf(oldPrice))
                            .addProperty("premium_price", String.valueOf(premiumPrice));
                    jsonBuilder.addInArr(priceBody, "prices");
                }
            });
            return executePostRequest("/v1/product/import/prices", jsonBuilder);
        });
    }

    public static OzonManager getInstance() {
        return OzonManagerHolder.OZON_MANAGER;
    }

    private static class OzonManagerHolder {
        public static final OzonManager OZON_MANAGER = new OzonManager();
    }

    public static void main(String[] args) {
        var x = new OzonManager();
        //var r = x.executePostRequest("/v1/warehouse/list");
        //List<Product> productsFromDb = ProductDAO.getInstance().getAll();
        Collection<Product> products = x.getProductsFromMP();
                //SupplierUtils.supplierFilter(productsFromDb, SUPPLIER_NAME.Xmarket);
        x.executeWithMaxItems(products, 100, productsForRequest -> {
            var res = x.updateProductStocksInWarehouse(productsForRequest, SUPPLIER_NAME.Xmarket.name(), WAREHOUSE_KUDROVO, 0);
            System.out.println("res = " + res);
            return res;
        });

    }


}
