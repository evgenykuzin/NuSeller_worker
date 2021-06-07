package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.product.YamarketProduct;

public class YamarketProductDAO extends AbstractDAO<YamarketProduct, String> {
    private YamarketProductDAO() {
        super(YamarketProduct.class);
    }

    @Override
    public String getTableName() {
        return "yamarket_products_extra";
    }

    public static YamarketProductDAO getInstance() {
        return YamarketDAOHolder.YAMARKET_PRODUCT_DAO;
    }

    private static class YamarketDAOHolder {
        public static final YamarketProductDAO YAMARKET_PRODUCT_DAO = new YamarketProductDAO();
    }
}
