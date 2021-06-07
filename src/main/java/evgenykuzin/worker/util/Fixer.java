package evgenykuzin.worker.util;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.db.dao.*;
import com.github.evgenykuzin.core.entities.*;
import com.github.evgenykuzin.core.entities.product.*;
import com.github.evgenykuzin.core.marketplace_api_integrations.yandex_market.YamarketManager;
import com.github.evgenykuzin.core.parser.*;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.core.util_managers.data_managers.CsvDataManager;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fixer {
    public static void main(String[] args) throws Exception {
        //updateYamarketPrices();
    }

//    private static void updatePricesInMPTables() {
//        ProductDAO.getInstance().getAll().forEach(product -> {
//            var ozon = product.getOzonProduct();
//            var yamarket = product.getYamarketProduct();
//            var wb = product.getWildeberriesProduct();
//            var price = product.getPrice();
//            System.out.println("start----------");
//            System.out.println("product = " + product);
//            System.out.println("ozon = " + ozon);
//            System.out.println("yamarket = " + yamarket);
//            System.out.println("wb = " + wb);
//            if (ozon != null) {
//                ozon.setPrice(price);
//                OzonProductDAO.getInstance().update(ozon);
//            }
//            if (yamarket != null) {
//                yamarket.setPrice(price);
//                YamarketProductDAO.getInstance().update(yamarket);
//            }
//            if (wb != null) {
//                wb.setPrice(price);
//                WildeberriesProductDAO.getInstance().update(wb);
//            }
//            System.out.println("finish---------");
//        });
//    }

    private static String fixIllegalCharsWB(String string) {
        StringBuilder sb = new StringBuilder();
        for (var ch : string.split("")) {
            if (ch.matches("[а-яА-ЯёЁ0-9a-zA-Z@!?,.|/:;\"&#$№%\\[\\]{}()+-]")) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static void fillDimensionIds() {
        ProductDAO.getInstance()
                .getAll()
                .stream()
                .filter(product -> product.getDimensions() == null)
                .forEach(product -> {
                    for (Dimensions dimensions : DimensionsDAO.getInstance().getAll()) {
                        if (dimensions.getProductId() == null) {
                            dimensions.setProductId(product.getId());
                            product.setDimensions(dimensions);
                            ProductDAO.getInstance().update(product);
                            DimensionsDAO.getInstance().update(dimensions);
                            break;
                        }
                    }
                });
    }

    private static void fillProductsTable() {
        var file = new File("C:\\Users\\JekaJops\\Downloads\\a0524484_nu_seller_ozon_products.csv");
        var charset = "utf-8";
        var idColName = "id";
        var separator = '|';
        var dataManager = new CsvDataManager(file, file, charset, idColName, separator);
        StockDAO stockDAO = StockDAO.getInstance();
        OzonProductDAO ozonProductDAO = OzonProductDAO.getInstance();
        ProductDAO productDAO = ProductDAO.getInstance();
        var existedProducts = productDAO.getAll();
        AtomicInteger dimIdCounter = new AtomicInteger(1);
        dataManager.parseTable().forEach((csvKey, row) -> {
            System.out.println("row = " + row);
            Product product = new Product();
            var article = row.get("article");
            var name = row.get("product_name");
            product.setArticle(article);
            product.setName(name);
            if (!article.equals("DELETED") && !name.equals("DELETED")) {
                Stock stock;
                var containedProduct = getContainedProduct(product, existedProducts);
                if (containedProduct == null) {
                    product.setBarcode(row.get("barcode"));
                    product.setBrandName(row.get("brand_name"));
                    product.setSupplierName(Enum.valueOf(SUPPLIER_NAME.class, row.get("supplier_name")));
                    var urls = row.get("urls");
                    if (urls != null && !urls.isEmpty()) {
                        product.setUrls(List.of(urls));
                    }
                    stock = new Stock();
                    Integer stockValue = parseField(row.get("stock"), 0, Integer::parseInt);
                    stock.setPositiveCount(stockValue);
                    stock.setNegativeCount(0);
                    stockDAO.saveOrUpdate(stock);
                    product.setStock(stock);
                    System.out.println("add new product" + product);
                } else {
                    product = containedProduct;
                    System.out.println("existed product = " + product);
                    stock = null;
                }
                if (product.getDimensions() == null) {
                    var dimId = (long) dimIdCounter.getAndIncrement();
                    System.out.println("dimId = " + dimId);
                    product.setDimensions(new Dimensions(dimId, 10.0, 10.0, 10.0, 1.0, product.getId()));
                }
                //product.setDimensions(new Dimensions(null, 10.0, 10.0, 10.0, 1.0, null));
                //product.setOzonProduct();
                if (product.getOzonProduct() == null) {
                    OzonProduct ozonProduct = new OzonProduct();
                    ozonProduct.setOzonId(row.get("id"));
                    Long categoryId = parseField(row.get("category_id"), null, Long::parseLong);
                    ozonProduct.setCategoryId(categoryId);
                    ozonProduct.setSkuFbs(row.get("sku_fbs"));
                    ozonProduct.setSkuFbo(row.get("sku_fbo"));
                    Double price = parseField(row.get("price"), null, Double::parseDouble);
                    product.setPrice(price);
                    ozonProductDAO.saveOrUpdate(ozonProduct);
                    product.setOzonProduct(ozonProduct);
                    productDAO.saveOrUpdate(product);
                    ozonProduct.setProductId(product.getId());
                    ozonProductDAO.update(ozonProduct);
                    System.out.println("ozonProduct = " + ozonProduct);
                }
//                Stock stock = new Stock();
//                Integer stockValue = parseField(row.get("stock"), 0, Integer::parseInt);
//                stock.setPositiveCount(stockValue);
//                stock.setNegativeCount(0);
//                stockDAO.saveOrUpdate(stock);
//                product.setStock(stock);

                if (stock != null) {
                    stock.setProductId(product.getId());
                    stockDAO.update(stock);
                    System.out.println("stock = " + stock);
                }
            }
        });

//        productDAO.saveAll(products.stream()
//                .filter(product -> !product.getArticle().equals("DELETED") && !product.getName().equals("DELETED"))
//                .collect(Collectors.toList()));
    }

    private static void fillYaProductsTable() {
        var file = new File("C:\\Users\\JekaJops\\Downloads\\a0524484_nu_seller_yamarket_products.csv");
        var charset = "utf-8";
        var idColName = "id";
        var separator = '|';
        var dataManager = new CsvDataManager(file, file, charset, idColName, separator);
        //StockDAO stockDAO = StockDAO.getInstance();
        YamarketProductDAO yamarketProductDAO = YamarketProductDAO.getInstance();
        ProductDAO productDAO = ProductDAO.getInstance();
        var existedProducts = productDAO.getAll();
        dataManager.parseTable().forEach((csvKey, row) -> {
            System.out.println("row = " + row);
            Product product = new Product();
            var article = row.get("article");
            var name = row.get("product_name");
            product.setArticle(article);
            product.setName(name);
            if (!article.equals("DELETED") && !name.equals("DELETED")) {
                var containedProduct = getContainedProduct(product, existedProducts);
                if (containedProduct != null) product = containedProduct;
                System.out.println("containedProduct = " + containedProduct);

                var barcode = row.get("barcode");
                if (barcode != null) product.setBarcode(barcode);
                var brandName = row.get("brand_name");
                if (brandName != null) product.setBrandName(brandName);
                var supName = row.get("supplier_name");
                if (supName != null) product.setSupplierName(Enum.valueOf(SUPPLIER_NAME.class, supName));
                var urls = row.get("urls");
                if (urls != null && !urls.isEmpty()) {
                    product.setUrls(List.of(urls));
                }

                //product.setDimensions(new Dimensions(null, 10.0, 10.0, 10.0, 1.0, null));
                //product.setOzonProduct();
                if (product.getYamarketProduct() == null) {
                    YamarketProduct yamarketProduct = new YamarketProduct();
                    yamarketProduct.setYamarketId(row.get("id"));
                    yamarketProduct.setCategory(row.get("category"));
                    yamarketProduct.setPackageStock(1);
                    Double price = parseField(row.get("price"), null, Double::parseDouble);
                    if (price != null) product.setPrice(price);
                    yamarketProductDAO.saveOrUpdate(yamarketProduct);
                    product.setYamarketProduct(yamarketProduct);
                    productDAO.saveOrUpdate(product);
                    yamarketProduct.setProductId(product.getId());
                    yamarketProductDAO.update(yamarketProduct);
                    System.out.println("yamarketProduct = " + yamarketProduct);
                }
//                Stock stock = new Stock();
//                Integer stockValue = parseField(row.get("stock"), 0, Integer::parseInt);
//                stock.setPositiveCount(stockValue);
//                stock.setNegativeCount(0);
//                stockDAO.saveOrUpdate(stock);
//                product.setStock(stock);

                productDAO.saveOrUpdate(product);
            }
        });

//        productDAO.saveAll(products.stream()
//                .filter(product -> !product.getArticle().equals("DELETED") && !product.getName().equals("DELETED"))
//                .collect(Collectors.toList()));
    }

    private static Product getContainedProduct(Product product, List<Product> products) {
        for (var p : products) {
            if (p.getArticle().equals(product.getArticle())) {
                if (p.getName().equals(product.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    private static <T> T parseField(String string, T defaultValue, Function<String, T> typeConverter) {
        return string == null || string.isEmpty() ? defaultValue : typeConverter.apply(string);
    }

    private static void updateBrands() {
        var zoo = new ZooekspressParser().parseProducts();
        var xmarket = new XmarketParser().parseProducts();

        var dao = ProductDAO.getInstance();
        var prds = dao.getAll();
        for (var p : prds) {
            if (p.getBrandName() == null || p.getBrandName().isEmpty()) {
                var supName = p.getSupplierName();
                switch (supName) {
                    case Zooekspress:
                        updateBrand(p, zoo);
                        break;
                    case Xmarket:
                        updateBrand(p, xmarket);
                        break;
                }
                if (p.getBrandName() != null && !p.getBrandName().isEmpty()) {
                    dao.update(p);
                }
            }
        }
        System.out.println("DONE.");
    }

    private static Product updateBrand(Product product, Collection<SupplierProduct> supProducts) {
        for (var supProd : supProducts) {
            if (product.getArticle().equals(supProd.getArticle())) {
                var brand = supProd.getBrandName();
                if (brand != null && !brand.isEmpty()) {
                    product.setBrandName(brand);
                    return product;
                }
            }
        }
        return product;
    }

    private static void updateYamarketPrices() {
        var yamanager = YamarketManager.getInstance();
        var dao = ProductDAO.getInstance();
        var prods = dao.getAll();
        var result = yamanager.updateProductsPrices(prods);
        System.out.println("result = " + result);
    }

    private static void fixYamarketBadNames() {
        var yamanager = YamarketManager.getInstance();
        var dao = ProductDAO.getInstance();
//        System.out.println("inited");
        var fixedDB = dao.getAll();
        //dao.updateAll(fixedDB);
//
//        System.out.println("db updated");

//        var fixedMP = yamanager.getProductsFromMP().stream()
//                .peek(getYamarketBadNamesConsumer())
//                .collect(Collectors.toSet());

        yamanager.importProductsToMP(fixedDB);
        System.out.println("mp updated");

    }

    private static Consumer<Product> getYamarketBadNamesConsumer() {
        return yamarketProduct -> {
            if (yamarketProduct.getName().contains("?") || yamarketProduct.getArticle().contains("?")) {
                yamarketProduct.setName("DELETED");
                yamarketProduct.setArticle("DELETED");
                System.out.println("yamarketProduct = " + yamarketProduct);
            }
        };
    }

//    private static void fixYamarketWarehouse() {
//        var ydao = YamarketProductDAO.getInstance();
//        var ydao2 = new YamarketProductDAO2();
//        var spbselleryamanager = new YamarketManager(YamarketManager.SPB_SELLER_WAREHOUSE_ID);
//        //var nuselleryamanager = new YamarketManager(YamarketManager.NU_SELLER_WAREHOUSE_ID);
//        //var spbsellerProducts = spbselleryamanager.getProductsFromMP();
//
//        var set = ydao2.getAll().stream()
//                .map(ProxyProduct::new)
//                .collect(Collectors.toSet());
//
//        for (var spbProd : set) {
//            var dbprod2 = spbProd.getYamarketProduct();
//            if (dbprod2 != null) {
//                var dbprod = new YamarketProduct(
//                        dbprod2.getId(),
//                        dbprod2.getName(),
//                        dbprod2.getBrandName(),
//                        dbprod2.getDescription(),
//                        dbprod2.getCategory(),
//                        dbprod2.getArticle(),
//                        dbprod2.getBarcode(),
//                        dbprod2.getPrice(),
//                        dbprod2.getStock(),
//                        dbprod2.getPackageStock(),
//                        dbprod2.getSupplierName(),
//                        dbprod2.getSupplierId(),
//                        dbprod2.getDimensions(),
//                        dbprod2.getUrls()
//                );
//                ydao.saveOrUpdate(dbprod);
//            }
//        }

    //var result = spbselleryamanager.importProductsToMP(prodsToImport);
    //System.out.println("result = " + result);
    // }

//    @Getter
//    @ToString
//    public static class ProxyProduct {
//        private final String article;
//        private final String name;
//        private final YamarketProduct2 yamarketProduct;
//
//        public ProxyProduct(YamarketProduct2 yamarketProduct) {
//            article = yamarketProduct.getArticle();
//            name = yamarketProduct.getName();
//            this.yamarketProduct = yamarketProduct;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            ProxyProduct that = (ProxyProduct) o;
//            return Objects.equals(article, that.article) && Objects.equals(name, that.name);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(article, name);
//        }
//    }

    private static void fixProductsCount() throws IOException {
        YamarketManager yamarketManager = YamarketManager.getInstance();
        //File stocksFile = new File("C:\\Users\\JekaJops\\Downloads\\stock.xlsx");
        Collection<Product> yamarketProducts = yamarketManager.getProductsFromMP();
        System.out.println("yamarketProducts = " + yamarketProducts.size());
//        DataManager dataManager = new XlsxDataManager(stocksFile, "Артикул") {
//            @Override
//            public void removeGarbageFromData(List<List<Object>> data) {
//            }
//        };
//        Collection<Product> productsFromStocks = dataManager.parseProductsList(row -> {
//            Product product = new OzonProduct();
//            product.setArticle(row.get("Артикул"));
//            product.setName(row.get("Наименование товара"));
//            return product;
//        });

        var productsFromDb = ProductDAO.getInstance().getAll();

        Collection<Product> container = new ArrayList<>();

        for (var stockProduct : productsFromDb) {
            var sarticle = stockProduct.getArticle();
            boolean exists = false;
            for (var mpProduct : yamarketProducts) {
                if (mpProduct.getArticle().equals(sarticle)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) container.add(stockProduct);
        }

        System.out.println("container = " + container.size());

        var res = yamarketManager.importProductsToMP(container);
        System.out.println("res = " + res);
    }

    private static void fixBarcodes() {
        var supplierManagers = SupplierParsersCollectionFactory.getSupplierParsers();
        for (var supplierManager : supplierManagers) {
            supplierManager.updateBarcodes();
        }
    }

    public static void fix() {
        var googleDocDataManager = DataManagerFactory.getOzonGoogleDocDataManager();
        var table = googleDocDataManager.parseTable();
        var resultMap = getResultMap(table);
        var xmarketFile = FileManager.getOrCreateIfNotExist("xmarketFromOzon", ".xlsx");
        var zooFile = FileManager.getOrCreateIfNotExist("zooekspressFromOzon", ".xlsx");
        var miragFile = FileManager.getOrCreateIfNotExist("myragToysFromOzon", ".xlsx");
        var xmName = new XmarketParser().getName();
        var zooName = new ZooekspressParser().getName();
        write(resultMap.get(xmName), xmarketFile);
        write(resultMap.get(zooName), zooFile);
    }

    public static Map<String, Table> getResultMap(Table table) {
        var resultSet = new HashMap<String, Table>();
        for (SupplierParser supplierParser : SupplierParsersCollectionFactory.getSupplierParsers()) {
            var resultSupTable = new Table(table.getIdKeyName(), table.getKeys().stream().map((Function<String, Object>) s -> s).collect(Collectors.toList()));
            var supProducts = supplierParser.parseProducts();
            for (Table.Row row : table.values()) {
                var ozonArticle = row.get(TableConfig.OzonDocConfig.ARTICLE_COL_NAME);
                for (SupplierProduct supProduct : supProducts) {
                    if (supProduct.getArticle() == null) {
                        System.out.println("supProduct with null article = " + supProduct);
                        continue;
                    }
                    if (supProduct.getArticle().equals(ozonArticle)) {
                        resultSupTable.put(row.get(TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME), row);
                        break;
                    }
                }
            }
            resultSet.put(supplierParser.getName().name(), resultSupTable);
        }
        return resultSet;
    }

    public static void write(Table table, File file) {
        var xlsDM = new XlsxDataManager(file, TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME) {
            @Override
            public void removeGarbageFromData(List<List<Object>> data) {
            }
        };
        xlsDM.writeAll(table);
    }

}
