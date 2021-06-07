package evgenykuzin.core.marketplace_api_integrations.yandex_market;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.Dimensions;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.YamarketProduct;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.Header;
import com.github.evgenykuzin.core.util.http.headers.HeadersModel;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.services.NetHttpService;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.PropertiesManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.*;

public class YamarketManager implements MPManager<Product>, Loggable {
    private static final Properties yandexMarketProps = PropertiesManager.getProperties("yamarket");
    private static final String YM_OAUTH_TOKEN = yandexMarketProps.getProperty("oauth.token");
    private static final String YM_OAUTH_CLIENT_ID = yandexMarketProps.getProperty("oauth.client_id");
    private static final int MAX_ITEMS_IN_REQUEST = 500;
    public static final String SPB_SELLER_WAREHOUSE_ID = "21941718";
    public static final String NU_SELLER_WAREHOUSE_ID = "22032433";
    private final String ymApiHost;
    private final NetHttpService httpService;

    private YamarketManager() {
        this(yandexMarketProps.getProperty("campaign-id"));
    }

    public YamarketManager(String ymCampaignId) {
        this.httpService = new NetHttpService();
        this.ymApiHost = String.format("https://api.partner.market.yandex.ru/v2/campaigns/%s/", ymCampaignId);
    }

    @Override
    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        HeadersModel headers = new HeadersModelImpl(
                new Header("Authorization", String.format("OAuth oauth_token=\"%s\", oauth_client_id=\"%s\"", YM_OAUTH_TOKEN, YM_OAUTH_CLIENT_ID)),
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Accept-Charset", "utf-8;q=0.7,*;q=0.7")
        );
        if (!mapping.contains(".json")) mapping = mapping + ".json";
        var req = httpService.constructRequest(
                ymApiHost + mapping,
                httpMethod,
                headers,
                bodyBuilder
        );
        JsonObject result;
        String responseString = httpService
                .getResponse(req)
                .getResponseString();
        result = new Gson()
                .fromJson(responseString, JsonObject.class);

        return result;
    }

    @Override
    public JsonObject updateProductStocks(Collection<Product> products, String supplierName) {
        return executeWithMaxItems(products, MAX_ITEMS_IN_REQUEST, productsForRequest -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            var arrName = "offerMappingEntries";
            jsonBuilder.addNewArr(arrName);
            for (var product : productsForRequest) {
                JsonObject jsonProduct = constructProductJsonToUpdate(product);
                jsonBuilder.addInArr(jsonProduct, arrName);
            }
            return executePostRequest("offer-mapping-entries/updates", jsonBuilder);
        });
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        Collection<JsonObject> resultJsonCollection = new ArrayList<>();
        JsonElement nextPageToken = null;
        do {
            String mapping = "offer-mapping-entries.json";
            JsonObject resp;
            if (nextPageToken != null) {
                var params = new HashMap<String, String>();
                params.put("page_token", nextPageToken.getAsString());
                resp = executeGetRequest(mapping, params);
            } else {
                resp = executeGetRequest(mapping);
            }
            JsonObject result = resp.getAsJsonObject("result");
            JsonArray jsonArray = result.getAsJsonArray("offerMappingEntries");
            var paging = result.getAsJsonObject("paging");
            nextPageToken = paging.get("nextPageToken");
            for (var je : jsonArray) {
                resultJsonCollection.add(je.getAsJsonObject());
            }
        } while (nextPageToken != null);

        return resultJsonCollection;
    }

    @Override
    public Product constructProduct(JsonObject jsonObject) {
        Product product = new Product();
        YamarketProduct yamarketProduct = product.getYamarketProduct();
        var offer = getJsonProp(jsonObject, "offer");
        product.setId(getLongProp(offer, "shopSku"));
        product.setName(getStrProp(offer, "name"));
        product.setBrandName(getStrProp(offer, "vendor"));
//        Product.setDescription(getStrProp(offer, "description"));
        product.setArticle(getStrProp(offer, "vendorCode"));
        var barcodes = offer.getAsJsonArray("barcodes");
        var barcode = barcodes == null || barcodes.size() == 0 ? null : barcodes.get(0).getAsString();
        product.setBarcode(barcode);
        yamarketProduct.setCategory(getStrProp(offer, "category"));
        product.setYamarketProduct(yamarketProduct);
        return product;
    }

    @Override
    public JsonObject constructJsonFromProduct(Product product) {
        YamarketProduct yamarketProduct = product.getYamarketProduct();
        if (yamarketProduct == null) return null;
        JsonObject result = new JsonObject();
        safeAddProperty(result, "name", product.getName());
        safeAddProperty(result, "vendor", product.getBrandName());
        safeAddProperty(result, "vendorCode", product.getArticle());
        var barcodesArray = new JsonArray();
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            barcodesArray.add(product.getBarcode());
            result.add("barcodes", barcodesArray);
        }
        safeAddProperty(result, "price", product.getPrice());
        return result;
    }

    @Override
    public List<Product> getOrderedProducts() {
        //TODO
        return null;
    }

    @Override
    public JsonObject importProductsToMP(Collection<Product> products) {
        return executeWithMaxItems(products, MAX_ITEMS_IN_REQUEST, productsForRequest -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            var arrName = "offerMappingEntries";
            jsonBuilder.addNewArr(arrName);
            for (var product : productsForRequest) {
                if (product.getBrandName() == null || product.getBrandName().isEmpty()) continue;
                JsonObject jsonSku = getRecomendedSkuJson(product, String.valueOf(product.getId()));
                var marketSku = getStrProp(jsonSku, "marketSku");
                var marketSkuName = getStrProp(jsonSku, "marketSkuName");
                var marketModelId = getStrProp(jsonSku, "marketModelId");
                var marketModelName = getStrProp(jsonSku, "marketModelName");
                var marketCategoryId = getStrProp(jsonSku, "marketCategoryId");
                var marketCategoryName = getStrProp(jsonSku, "marketCategoryName");
                JsonObject jsonProduct = new JsonBuilder(constructProductJsonToUpdate(product))
                        .addNewObj("mapping")
                        .addPropertyInObj("marketSku", marketSku, "mapping")
                        .build()
                        .getJsonObject();
                jsonBuilder.addInArr(jsonProduct, arrName);

            }
            return executePostRequest("offer-mapping-entries/updates", jsonBuilder);
        });
    }

    @Override
    public MP_NAME getName() {
        return MP_NAME.YAMARKET;
    }

    @Override
    public Collection<Product> getProductsFromMP() {
        return MPManager.super.getProductsFromMP().stream()
                .map(ProxyProduct::new)
                .distinct()
                .map(ProxyProduct::getProduct)
                .collect(Collectors.toSet());
    }


    private JsonObject constructProductJsonToUpdate(Product product) {
        YamarketProduct yamarketProduct = product.getYamarketProduct();
        var offerObjName = "offer";
        JsonBuilder importProductBuilder = new JsonBuilder();
        var shopSku = String.valueOf(product.getId());
        var name = product.getName();
        var brandName = product.getBrandName();
        var article = product.getArticle();
        var barcode = product.getBarcode();
        var supplierName = product.getSupplierName();
        //var price = product.getPrice();
        var stock = product.getStock().computeStock();
        stock = stock == null ? 0 : stock;
        var packageStock = yamarketProduct.getPackageStock();
        packageStock = packageStock == null ? 1 : packageStock;
        var minShipment = String.valueOf(stock * packageStock);
        var urls = product.getUrls();
        var dimensions = product.getDimensions();
        if (dimensions == null) dimensions = new Dimensions(null, 10.0, 10.0, 10.0, 1.0, product.getId());
        var category = yamarketProduct.getCategory();
        var barcodesArray = new JsonArray();
        barcodesArray.add(barcode);
        var urlsArray = new JsonArray();
        for (var url : urls) {
            urlsArray.add(url);
        }
        var weightDimensions = new JsonObject();
        weightDimensions.addProperty("length", dimensions.getLength());
        weightDimensions.addProperty("width", dimensions.getWidth());
        weightDimensions.addProperty("height", dimensions.getHeight());
        weightDimensions.addProperty("weight", dimensions.getWeight());
        var manufacturerCountriesArray = new JsonArray();
        manufacturerCountriesArray.add(getCountryByBrand(brandName));

        importProductBuilder
                .addNewObj(offerObjName)
                .addPropertyInObj("shopSku", shopSku, offerObjName)
                .addPropertyInObj("name", name, offerObjName)
                .addPropertyInObj("category", category, offerObjName)
                .addPropertyInObj("vendor", brandName, offerObjName)
                .addPropertyInObj("vendorCode", article, offerObjName)
                .addPropertyInObj("manufacturer", brandName, offerObjName)
                .addObjInObj("weightDimensions", weightDimensions, offerObjName)
                .addArrInObj("barcodes", barcodesArray, offerObjName)
                .addArrInObj("urls", urlsArray, offerObjName)
                .addArrInObj("manufacturerCountries", manufacturerCountriesArray, offerObjName);

        return importProductBuilder.build().getJsonObject();
    }

    public JsonObject updateProductsPrices(Collection<Product> products) {
        return executeWithMaxItems(products, MAX_ITEMS_IN_REQUEST, productsForRequest -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            var offersArrName = "offers";
            var priceObjName = "price";
            JsonBuilder jsonBuilder = new JsonBuilder()
                    .addNewArr(offersArrName);
            for (var product : productsForRequest) {
                YamarketProduct yamarketProduct = product.getYamarketProduct();
                if (yamarketProduct == null) continue;
                var price = product.getPrice();
                if (price == null) continue;
                var discBase = (double) Math.round(price + ((price / 100) * 30));
                var jsonSku = getRecomendedSkuJson(product, String.valueOf(product.getId()));
                var marketSku = getStrProp(jsonSku, "marketSku");
                if (marketSku != null) {
                    jsonBuilder
                            .addInArr(new JsonBuilder()
                                    .addProperty("marketSku", marketSku)
                                    .addNewObj(priceObjName)
                                    .addPropertyInObj("currencyId", "RUR", priceObjName)
                                    .addPropertyInObj("value", String.valueOf(price), priceObjName)
                                    .addPropertyInObj("discountBase", String.valueOf(discBase), priceObjName), offersArrName);
                }
            }
            return executePostRequest("offer-prices/updates", jsonBuilder);
        });
    }

    public JsonObject getRecomendedSkuJson(Product product, String shopSku) {
        JsonObject jsonProduct = constructJsonFromProduct(product);
        if (jsonProduct == null) return new JsonObject();
        jsonProduct.addProperty("shopSku", shopSku);
        var response = executePostRequest("offer-mapping-entries/suggestions", new JsonBuilder()
                .addNewArr("offers")
                .addInArr(new JsonBuilder(jsonProduct), "offers"));
        var result = response.getAsJsonObject("result");
        if (result == null) return new JsonObject();
        var offers = result.getAsJsonArray("offers");
        if (offers == null || offers.size() == 0) return new JsonObject();
        return offers.get(0).getAsJsonObject();
    }

    private static String getCountryByBrand(String brand) {
        String defaultCountry = "Россия";
        if (brand == null || brand.isEmpty()) return defaultCountry;
        Map<String, String> map = new HashMap<>();
        var data =
                "Trixie - Германия\n" +
                        "V.I.Pet - Китай\n" +
                        "Ecopet - Сербия\n" +
                        "Зооник - Россия \n" +
                        "Beeztees - Нидерланды\n" +
                        "DeLIGHT - Тайвань\n" +
                        "WAUDOG - Украина\n" +
                        "Юг-Пласт - Россия\n" +
                        "Полесье - Беларусь\n" +
                        "МагМастер - Россия\n" +
                        "Zoo One - Китай\n" +
                        "Zooexpress - Россия\n" +
                        "Collar - Украина\n" +
                        "WINYI - Китай\n" +
                        "Diavolo - Англия\n" +
                        "Rocks-off - Англия\n" +
                        "Cosmopolitan - Китай\n" +
                        "Pixey - Нидерланды\n" +
                        "Hustler - Россия\n" +
                        "ML Creation - Китай";
        Arrays.stream(data.split("\n")).forEach(s -> {
            var split = s.split(" - ");
            map.put(split[0].toLowerCase(), split[1]);
        });
        var result = map.get(brand.toLowerCase());
        if (result == null) result = defaultCountry;
        return result;
    }

    public static YamarketManager getInstance() {
        return YamarketManagerHolder.YAMARKET_MANAGER;
    }

    private static class YamarketManagerHolder {
        public static final YamarketManager YAMARKET_MANAGER = new YamarketManager();
    }

    @Getter
    @ToString
    public static class ProxyProduct {
        private final String article;
        private final String name;
        private final Product product;

        public ProxyProduct(Product product) {
            article = product.getArticle();
            name = product.getName();
            this.product = product;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProxyProduct that = (ProxyProduct) o;
            return Objects.equals(article, that.article) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(article, name);
        }
    }



    public static void main(String[] args) {
        var ym = new YamarketManager();
        var dao = ProductDAO.getInstance();
        //var product = dao.get(1002714048L);
        var products = dao.getAll();
        System.out.println("products = " + products);
        //var stocks = new ZooekspressParser().parseNewStocksProducts(products.stream().map(p->(Product)p).collect(Collectors.toList()));
        //var res = ym.updateProductStocks(products, "");
        //System.out.println("res = " + res);
        //ym.getProductsJsonsFromMP();
        var res = ym.updateProductsPrices(products);
        System.out.println("res = " + res);
    }

}
