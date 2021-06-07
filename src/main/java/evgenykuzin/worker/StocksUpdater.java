package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.db.dao.StockDAO;
import com.github.evgenykuzin.core.db.dao.WildeberriesProductDAO;
import com.github.evgenykuzin.core.entities.Stock;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.entities.product.WildeberriesProduct;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.parser.SupplierParser;
import com.github.evgenykuzin.core.parser.SupplierParsersCollectionFactory;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManager;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.parser.SupplierUtils.supplierFilter;

public class StocksUpdater implements Loggable {
    private SupplierParser supplierParser;
    private final ProductDAO productDAO;
    private final StockDAO stockDAO;

    public StocksUpdater() {
        this.productDAO = ProductDAO.getInstance();
        stockDAO = StockDAO.getInstance();
    }

    public void updateStocks(MPManager<Product> mpManager) {
        try {
            List<Product> products = productDAO.getAll();
            for (SupplierParser supplierParser : SupplierParsersCollectionFactory.getSupplierParsers()) {
                this.supplierParser = supplierParser;
                //updateStocksInDB(supplierParser, products);
                Collection<Product> updatedStocksSupProducts = getUpdatedStocksProducts(supplierParser, products)
                        .stream()
                        .map(supplierProduct -> {
                            var product = ProductDAO.getInstance().get(supplierProduct.getProductId());
                            Integer stock = supplierProduct.getStock();
                            if (mpManager.getName().equals(MP_NAME.WILDEBERRIES)) {
                                System.out.println("stock = 0 for WB");
                                stock = 0;
                            }
                            product.setStock(Stock.tempStock(stock));
                            return product;
                        })
                        .collect(Collectors.toList());
                updateStocksWithApi(updatedStocksSupProducts, mpManager);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private List<SupplierProduct> getUpdatedStocksProducts(SupplierParser supplierParser, List<Product> products) {
        List<Product> filtered = supplierFilter(products, supplierParser.getName());
        System.out.println("products was filtered");
        Collection<SupplierProduct> updatedStocksSupProducts = supplierParser
                .parseSupplierProducts(filtered);
        System.out.println("new stocks parsed");
        return updatedStocksSupProducts.stream()
                .peek(supplierProduct -> {
                    var stockValue = supplierProduct.getStock();
                    if (stockValue > 0) {
                        stockValue -= 1;
                    } else {
                        stockValue = 0;
                    }
                    supplierProduct.setStock(stockValue);
                })
                .collect(Collectors.toList());
    }

    public void updateStocksWithApi(Collection<Product> products, MPManager<Product> mpManager) {
//        var updatedStocksProducts = getUpdatedStocksProducts();
//        logf("updated stocks count: %d for supplier %s", updatedStocksProducts.size(), supplierParser.getName());
//        if (updatedStocksProducts.isEmpty()) {
//            logf("supplier %s has no products to parse (may be because of a supplierFilter)", supplierParser.getName());
//            return;
//        }
        mpManager.updateProductStocks(products, supplierParser.getName().name());
        logf("stocks for supplier %s was updated", supplierParser.getName());
    }

    public void updateStocksInDB(SupplierParser supplierParser, List<Product> products) {
        Collection<SupplierProduct> updatedStocksSupProducts = getUpdatedStocksProducts(supplierParser, products);
        updatedStocksSupProducts.forEach(supplierProduct -> {
            Stock stock = stockDAO.getByProductId(supplierProduct.getProductId());
            if (stock == null) {
                stock = new Stock();
                stock.setNegativeCount(0);
                stock.setProductId(supplierProduct.getProductId());
                stockDAO.save(stock);
                Product product = productDAO.get(supplierProduct.getProductId());
                product.setStock(stock);
                productDAO.update(product);
            }
            stock.setPositiveCount(supplierProduct.getStock());
            System.out.println("stock = " + stock);
            stockDAO.update(stock);
        });
    }

    public static StocksUpdater getInstance() {
        return StocksUpdaterHolder.STOCKS_UPDATER;
    }

    private static class StocksUpdaterHolder {
        public static final StocksUpdater STOCKS_UPDATER = new StocksUpdater();
    }

    public static void main(String[] args) {
        var file = FileManager.getFromResources("Отчет по скидкам.xlsx");
        String keyColName = "Номенклатура (код 1С)";
        DataManager dataManager = XlsxDataManager
                .getDefaultXslsDataManager(file, keyColName);
        Table table = dataManager.parseTable();
        var wbDAO = WildeberriesProductDAO.getInstance();
        var productDAO = ProductDAO.getInstance();
        Set<Integer> blackSet = Set.of(
                21350967,
                21350659,
                21057498,
                20870589,
                20869841,
                20862011,
                19337395
        );
        table.forEach((s, row) -> {
            Integer id = Integer.valueOf(parseField(s));
            if (!blackSet.contains(id)) {
                WildeberriesProduct wbProduct = wbDAO.get(id);
                if (wbProduct != null) {
                    Product product = null;
                    if (wbProduct.getProductId() != null) {
                        product = productDAO.get(wbProduct.getProductId());
                    }
                    if (product != null) {
                        row.put("Новая розн. цена (до скидки)", String.valueOf(product.getPrice().intValue()));
                    } else System.out.println("product = " + product);
                }
            }
        });
        dataManager.writeAll(table);
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
