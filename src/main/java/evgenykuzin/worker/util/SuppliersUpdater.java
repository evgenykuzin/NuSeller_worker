package evgenykuzin.worker.util;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.parser.SupplierParsersCollectionFactory;
import com.github.evgenykuzin.core.util.loger.Loggable;

public class SuppliersUpdater implements Loggable {
    public static void main(String[] args) {
        var dao = ProductDAO.getInstance();
        var ozonProductsFomDb = dao.getAll();
        SupplierParsersCollectionFactory.getSupplierParsers().forEach(supplierParser -> {
            var supplierProducts = supplierParser.parseProducts();
            for (SupplierProduct supplierProduct : supplierProducts) {
                for (Product product : ozonProductsFomDb) {
                    var supplierName = product.getSupplierName();
                    if (supplierName == null || supplierName.name().isEmpty()) {
                        if (product.getArticle().equals(supplierProduct.getArticle())) {
                            product.setSupplierName(supplierProduct.getSupplierName());
                            dao.update(product);
                        }
                    }
                }
            }
            LogConfig.logger.log(String.format("'suppliers' column for %s was updated", supplierParser.getName()));
        });
    }
}
