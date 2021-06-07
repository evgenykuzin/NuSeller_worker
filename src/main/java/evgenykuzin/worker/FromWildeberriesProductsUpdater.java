package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.wildeberries.WildeberriesManager;

public class FromWildeberriesProductsUpdater extends ProductsUpdaterFromMP {

    private FromWildeberriesProductsUpdater() {
        super(WildeberriesManager.getInstance(), ProductDAO.getInstance());
    }

    @Override
    public void additionalDbUpdates(Product updatedProduct, Product otherProduct) {
        var updatedWBProduct = updatedProduct.getWildeberriesProduct();
        var otherWBProduct = otherProduct.getWildeberriesProduct();
        if(updatedWBProduct == null) return;
        if (updatedWBProduct.getCategory() == null) {
            updatedWBProduct.setCategory(otherWBProduct.getCategory());
        }
        if (updatedWBProduct.getChrtId() == null) {
            updatedWBProduct.setChrtId(otherWBProduct.getChrtId());
        }
        if (updatedWBProduct.getProductId() == null) {
            updatedWBProduct.setProductId(updatedProduct.getId());
        }
        updatedProduct.setWildeberriesProduct(updatedWBProduct);
    }

    public static FromWildeberriesProductsUpdater getInstance() {
        return FromOzonProductsUpdaterHolder.FROM_OZON_PRODUCTS_UPDATER;
    }

    private static class FromOzonProductsUpdaterHolder {
        public static final FromWildeberriesProductsUpdater FROM_OZON_PRODUCTS_UPDATER = new FromWildeberriesProductsUpdater();
    }

}
