package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.yandex_market.YamarketManager;

public class FromYamarketProductsUpdater extends ProductsUpdaterFromMP {

    private FromYamarketProductsUpdater() {
        super(YamarketManager.getInstance(), ProductDAO.getInstance());
    }

    @Override
    public void additionalDbUpdates(Product updatedProduct, Product otherProduct) {
        var updatedYaProduct = updatedProduct.getYamarketProduct();
        var otherYaProduct = otherProduct.getYamarketProduct();
        if(updatedYaProduct == null) return;
        if (updatedProduct.getPrice() != null && otherYaProduct != null && otherProduct.getPrice() != null) {
            updatedProduct.setPrice(otherProduct.getPrice());
        }
    }

    public static FromYamarketProductsUpdater getInstance() {
        return FromYamarketProductsUpdaterHolder.FROM_YAMARKET_PRODUCTS_UPDATER;
    }

    private static class FromYamarketProductsUpdaterHolder {
        public static final FromYamarketProductsUpdater FROM_YAMARKET_PRODUCTS_UPDATER = new FromYamarketProductsUpdater();
    }

}
