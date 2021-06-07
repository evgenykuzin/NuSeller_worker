package evgenykuzin.core.util_managers.data_managers;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.entities.Stock;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.OzonProduct;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.parser.SUPPLIER_NAME;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.apache.xalan.xsltc.runtime.BasisLibrary.stringToInt;

public class DataManagerFactory {

    private DataManagerFactory() { }

    private static GoogleDocDataManager googleDocDataManager;

    public synchronized static GoogleDocDataManager getOzonGoogleDocDataManager() {
        if (googleDocDataManager == null) {
            try {
                Comparator<Table.Row> diffPricesComparator = Comparator.comparingDouble(DataManagerFactory::getDiffPricesInt);
                Comparator<Table.Row> supplierComparator = (o1, o2) -> {
                    var key = TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME;
                    var a = o1.get(key);
                    var b = o2.get(key);
                    if (stringToInt(a) == stringToInt(b)) {
                        return a.compareTo(b);
                    }
                    return stringToInt(b) - stringToInt(a);
                };
                googleDocDataManager = new GoogleDocDataManager("credentials.json",
                        "1E3VpDsMzSJe1hVbzf2Kz-T3sVzQAG8P_YGOQwtrNgrc",
                        TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME,
                        null,
                        row -> {
                        },
                        row -> {
                            row.putIfAbsent(TableConfig.AdditionalOzonDocFieldsConfig.LOWER_PRICE_COL_NAME, "");
                            row.putIfAbsent(TableConfig.AdditionalOzonDocFieldsConfig.DIFF_PRICES_COL_NAME, "");
                            row.putIfAbsent(TableConfig.AdditionalOzonDocFieldsConfig.CONCURRENT_URL_COL_NAME, "");
                            row.putIfAbsent(TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME, "");
                            row.putIfAbsent(TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME, "");
                        }
                ) {
                    @Override
                    public List<Product> parseProducts() {
                        return parseProductsList(row -> {
                            var priceStr = row.get(TableConfig.OzonDocConfig.PRICE_COL_NAME);
                            var price = priceStr == null || priceStr.isEmpty() ? null : Double.parseDouble(priceStr);
                            var name = row.get(TableConfig.OzonDocConfig.NAME_COL_NAME);
                            var brand = row.get(TableConfig.OzonDocConfig.BRAND_COL_NAME);
                            var barcode = row.get(TableConfig.OzonDocConfig.BARCODE_COL_NAME);
                            var article = row.get(TableConfig.OzonDocConfig.ARTICLE_COL_NAME);
                            var concUrl = row.get(TableConfig.AdditionalOzonDocFieldsConfig.CONCURRENT_URL_COL_NAME);
                            var concPriceStr = row.get(TableConfig.AdditionalOzonDocFieldsConfig.LOWER_PRICE_COL_NAME);
                            var concPrice = concPriceStr == null || concPriceStr.isEmpty() ? null : Double.parseDouble(concPriceStr);
                            var ozonProductId = row.get(TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME);
                            var stocksStr = row.get(TableConfig.OzonDocConfig.STOCKS_COL_NAME);
                            var stocks = stocksStr == null || stocksStr.isEmpty() ? 0 : Integer.parseInt(stocksStr);
                            var supplier = row.get(TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME);
                            var sku_fbs = row.get(TableConfig.OzonDocConfig.SKU_FBS_COL_NAME);
                            var sku_fbo = row.get(TableConfig.OzonDocConfig.SKU_FBO_COL_NAME);
                            Product product = new Product();
                            product.setName(name);
                            product.setBrandName(brand);
                            product.setArticle(article);
                            product.setBarcode(barcode);
                            product.setStock(Stock.tempStock(stocks));
                            product.setSupplierName(Enum.valueOf(SUPPLIER_NAME.class, supplier));
                            var ozonProduct = new OzonProduct();
                            ozonProduct.setOzonId(ozonProductId);
                            ozonProduct.setSkuFbs(sku_fbs);
                            ozonProduct.setSkuFbo(sku_fbo);
                            product.setPrice(price);
                            product.setOzonProduct(ozonProduct);
                            return product;
                        });
                    }
                };
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return googleDocDataManager;
    }

    public synchronized static GoogleDocDataManager getOzonGoogleDocDataManagerForView(String idColName) {
        return new GoogleDocDataManager(
                "credentials.json",
                "1nvKAb_ZVJFSXyMZbe1DZ2tNOkZmXsqJNP7Ck0zzconQ",
                idColName,
                null,
                row -> {
                },
                row -> {
                }
        ) {
            @Override
            public List<Product> parseProducts() {
                return null;
            }
        };
    }

        public static CsvDataManager getCsvManager(File inFile, String keyColName) {
        return new CsvDataManager(inFile, inFile, "windows-1251", keyColName, ';');
    }

    public static CsvDataManager getCsvManager(File inFile, String keyColName, char separator) {
        return new CsvDataManager(inFile, inFile, "windows-1251", keyColName, separator);
    }

    private static double getDiffPricesInt(Map<String, String> map) {
        var str = map.get(TableConfig.AdditionalOzonDocFieldsConfig.DIFF_PRICES_COL_NAME);
        if (str.isEmpty()) return 0;
        return Double.parseDouble(str);
    }

}
