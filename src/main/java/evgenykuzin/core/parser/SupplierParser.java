package evgenykuzin.core.parser;

import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManagerFactory;

import java.util.Collection;
import java.util.List;

import static com.github.evgenykuzin.core.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME;

public interface SupplierParser {
    SUPPLIER_NAME getName();
    List<SupplierProduct> parseSupplierProducts(List<Product> products);
    List<SupplierProduct> parseProducts();
    boolean sendOrders(Collection<? extends Product> products);


    default void updateBarcodes() {
        var webCsvDataManager = DataManagerFactory.getOzonGoogleDocDataManager();
        Table productsTable = webCsvDataManager.parseTable();
        var ozonProducts = webCsvDataManager.parseProducts();
        var supplierProducts = parseProducts();
        ozonProducts.stream()
                .filter(product -> product.getSupplierName().equals(getName()))
                .forEach(ozonProduct -> {
                    var ozonArticle = ozonProduct.getArticle();
                    for (var supplierProduct : supplierProducts) {
                        if (supplierProduct.getArticle().equals(ozonArticle)) {
                            var supplierBarcode = supplierProduct.getBarcode();
                            var id = String.valueOf(ozonProduct.getId());
                            productsTable.updateRowValue(id, SEARCH_BARCODE_COL_NAME, supplierBarcode);
                            break;
                        }
                    }
                });
        webCsvDataManager.writeAll(productsTable);
    }
}
