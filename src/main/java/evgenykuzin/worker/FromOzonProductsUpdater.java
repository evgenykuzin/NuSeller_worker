package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManager;

import java.util.Collection;

public class FromOzonProductsUpdater extends ProductsUpdaterFromMP {

    private FromOzonProductsUpdater() {
        super(OzonManager.getInstance(), ProductDAO.getInstance());
    }

    @Override
    public void additionalDbUpdates(Product updatedProduct, Product otherProduct) {
        var updatedOzonProduct = updatedProduct.getOzonProduct();
        var otherOzonProduct = otherProduct.getOzonProduct();
        if(updatedOzonProduct == null) return;
        if ((updatedOzonProduct.getSkuFbs() == null
                || updatedOzonProduct.getSkuFbs().isEmpty()
                || updatedOzonProduct.getSkuFbs().contains("+")
                || updatedOzonProduct.getSkuFbs().contains(","))
                && otherOzonProduct != null && otherOzonProduct.getSkuFbs() != null) {
            updatedOzonProduct.setSkuFbs(otherOzonProduct.getSkuFbs());
        }
        if (updatedProduct.getPrice() != null && otherOzonProduct != null && otherProduct.getPrice() != null) {
            updatedProduct.setPrice(otherProduct.getPrice());
        }
        if (updatedOzonProduct.getProductId() == null) {
            updatedOzonProduct.setProductId(updatedProduct.getId());
        }
        updatedProduct.setOzonProduct(updatedOzonProduct);
    }

    private static String getStr(Object object) {
        if (object == null) return "";
        return String.valueOf(object);
    }

    private static void fillEmpty(Table.Row row, Collection<String> keys) {
        for (String key : keys) {
            row.putIfAbsent(key, "");
        }
    }

    public static FromOzonProductsUpdater getInstance() {
        return FromOzonProductsUpdaterHolder.FROM_OZON_PRODUCTS_UPDATER;
    }

    private static class FromOzonProductsUpdaterHolder {
        public static final FromOzonProductsUpdater FROM_OZON_PRODUCTS_UPDATER = new FromOzonProductsUpdater();
    }

}
