package evgenykuzin.core.marketplace_api_integrations.wildeberries;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.WildeberriesProduct;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil;
import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.util.http.services.HttpService;
import com.github.evgenykuzin.core.util_managers.FTPManager;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;
import com.google.api.client.util.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class WildeberriesManager implements MPManager<Product> {
    private static final Integer storeId = 4345;

    @Override
    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        var service = new ClosableHttpService();

        var req = service.constructRequest(
                "https://wbxgate.wildberries.ru/" + mapping,
                httpMethod,
                new HeadersModelImpl(),
                bodyBuilder
        );
        var responseString = service.getResponse(req).getResponseString();
        var result = new Gson()
                .fromJson(responseString, JsonObject.class);
        System.out.println("result = " + result);
        return result;
    }

    @Override
    public JsonObject updateProductStocks(Collection<Product> products, String supplierName) {
        return executeWithMaxItems(products, 500, productsForRequest -> {
            var json = new MPUtil.JsonBuilder()
                    .addProperty("token", TOKEN)
                    .addNewArr("data");
            for (var product : productsForRequest) {
                WildeberriesProduct wildeberriesProduct = product.getWildeberriesProduct();
                if (wildeberriesProduct == null) continue;
                Integer wbId = wildeberriesProduct.getWbId();
                json.addInArr(new MPUtil.JsonBuilder()
                                .addIntProperty("nmId", wbId)
                                .addNewArr("stocks")
                                .addInArr(new MPUtil.JsonBuilder()
                                        .addIntProperty("chrtId", Integer.valueOf(wildeberriesProduct.getChrtId()))
                                        .addIntProperty("price", product.getPrice().intValue())
                                        .addIntProperty("quantity", product.getStock().computeStock())
                                        .addIntProperty("storeId", storeId), "stocks")
                        , "data");
            }
            System.out.println("json = " + json);

            return executePostRequest("stocks", json);
        });
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        var service = new ClosableHttpService();
        var headers = new HeadersModelImpl("Accept: */*\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7\n" +
                "Connection: keep-alive\n" +
                "Content-type: application/json\n" +
                "Cookie: ___wbu=33a882b3-b641-4f03-9905-c8bfd67bb4d7.1619286787; _wbauid=6803205911619286790; _gcl_au=1.1.404782986.1619286791; _gcl_aw=GCL.1619464498.Cj0KCQjwyZmEBhCpARIsALIzmnJPsMSgTMn2zmvDTV17ghqZC4ZRwO32jWf5_-3qtAsnWN4nW-kAhyEaAnJeEALw_wcB; _gac_UA-2093267-1=1.1619464499.Cj0KCQjwyZmEBhCpARIsALIzmnJPsMSgTMn2zmvDTV17ghqZC4ZRwO32jWf5_-3qtAsnWN4nW-kAhyEaAnJeEALw_wcB; _ga=GA1.2.745240075.1619286791; _ga_TW9NLWX9V5=GS1.1.1619472894.2.0.1619472894.60; __ac=true; x-supplier-id=2a980820-a7f7-5e58-b71f-4d733aa770e4; __store=119261_122252_122256_121631_122466_122467_122495_122496_122498_122590_122591_122592_123816_123817_123818_123820_123821_123822_124583_124584_117734_2737_117544_120762_119400_116433_117501_507_3158_117986_119742_117413_119781; __region=64_4_38_30_33_70_1_22_31_66_40_69_80_48_68; __pricemargin=1.0--; __cpns=12_6_7_5_3_18_21; __sppfix=; __spp=0; __catalogOptions=Sort%3APopular%26CardSize%3Ac246x328; _gid=GA1.2.1243725495.1620922173; ___wbs=1ac5a20c-cfa7-41a5-8022-28283c81f36e.1620922172; ncache=119261_122252_122256_121631_122466_122467_122495_122496_122498_122590_122591_122592_123816_123817_123818_123820_123821_123822_124583_124584_117734_2737_117544_120762_119400_116433_117501_507_3158_117986_119742_117413_119781%3B64_4_38_30_33_70_1_22_31_66_40_69_80_48_68%3B1.0--%3B12_6_7_5_3_18_21%3B%3B0%3BSort%3APopular%26CardSize%3Ac246x328%3Btrue; WBToken=AseA8hj-vOqJDP6mtIoMQkS7jUK49iJPycLjMkl5Be-u5GB8oWr5ZXgkWhXIYk6Sz9oRUDWCQQJDRkstDVs0bPMdmCUeYKXJEhNvmgn62UYpIQ\n" +
                "Host: seller.wildberries.ru\n" +
                "Referer: https://seller.wildberries.ru/marketplace-remains-of-goods-new/remains\n" +
                "sec-ch-ua: \"Chromium\";v=\"90\", \"Opera\";v=\"76\", \";Not A Brand\";v=\"99\"\n" +
                "sec-ch-ua-mobile: ?0\n" +
                "Sec-Fetch-Dest: empty\n" +
                "Sec-Fetch-Mode: cors\n" +
                "Sec-Fetch-Site: same-origin\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 OPR/76.0.4017.107");
        var req = service.constructRequest(
                "https://seller.wildberries.ru/ns/nomenclatures/analytics-back/api/v1/nomenclatures-excel",
                HttpService.POST,
                headers,
                new MPUtil.JsonBuilder().build()
        );
        String stringData = JsonParser.parseString(service.getResponse(req).getResponseString())
                .getAsJsonObject()
                .getAsJsonObject("data")
                .get("excelReportNomenclatures")
                .getAsString();
        byte[] byteData = Base64.decodeBase64(stringData);
        File wbFile = Objects.requireNonNull(FTPManager.getFileFromSuppliers("wb_products", ".xlsx"));
        try (OutputStream stream = new FileOutputStream(wbFile)) {
            stream.write(byteData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return XlsxDataManager
                .getDefaultXslsDataManager(wbFile, "Артикул WB")
                .parseTable()
                .convertToJsonCollection();
    }

    @Override
    public Product constructProduct(JsonObject jsonObject) {
        ProductDAO productDAO = ProductDAO.getInstance();
        String article = parseField(jsonObject.get("Артикул ИМТ").getAsString());
        String brandName = safeGet(jsonObject.get("Бренд"));
        Double price = Double.valueOf(safeGet(jsonObject.get("Розничная цена, руб")));
        Product product = productDAO.getByArticleAndBrand(article, brandName);
        if (product == null) {
            product = new Product();
            product.setArticle(article);
            product.setBrandName(brandName);
            product.setPrice(price);
            ProductDAO.getInstance().save(product);
        }
        WildeberriesProduct wildeberriesProduct = new WildeberriesProduct();
        var wbIdStr = parseField(jsonObject.get("Артикул WB"));
        wildeberriesProduct.setWbId(Integer.valueOf(wbIdStr));
        wildeberriesProduct.setCategory(safeGet(jsonObject.get("Предмет")));
        wildeberriesProduct.setChrtId(parseField(jsonObject.get("Код размера (chrt_id)")));
        wildeberriesProduct.setProductId(product.getId());
        product.setWildeberriesProduct(wildeberriesProduct);
        return product;
    }

    @Override
    public JsonObject constructJsonFromProduct(Product product) {
        return null;
    }

    @Override
    public List<Product> getOrderedProducts() {
        return null;
    }

    @Override
    public JsonObject importProductsToMP(Collection<Product> products) {
        return null;
    }

    @Override
    public MP_NAME getName() {
        return MP_NAME.WILDEBERRIES;
    }

    public static WildeberriesManager getInstance() {
        return WildeberriesManagerHolder.WILDEBERRIES_MANAGER;
    }

    private static class WildeberriesManagerHolder {
        public static final WildeberriesManager WILDEBERRIES_MANAGER = new WildeberriesManager();
    }

    public static void main(String[] args) {
        //List<Product> products = ProductDAO.getInstance().getAll();
        WildeberriesManager wildeberriesManager = new WildeberriesManager();
        System.out.println(wildeberriesManager.getProductsFromMP());
    }

    private static String parseField(String field) {
        if (field == null) return null;
        if (field.contains(".") && field.contains("E7")) {
            field = field
                    .replaceAll("\\.", "")
                    .replaceAll("E7", "");
        }
        return field;
    }

    private static String parseField(JsonElement element) {
        return parseField(safeGet(element));
    }

    private static String safeGet(JsonElement element) {
        if (element == null) return null;
        return element.getAsString();
    }
}
