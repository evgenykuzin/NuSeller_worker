package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.product.WildeberriesProduct;

public class WildeberriesProductDAO extends AbstractDAO<WildeberriesProduct, Integer> {
    private WildeberriesProductDAO() {
        super(WildeberriesProduct.class);
    }

    @Override
    public String getTableName() {
        return "wildeberries_products_extra";
    }

    public static WildeberriesProductDAO getInstance() {
        return WildeberriesProductDAOHolder.WILDEBERRIES_PRODUCT_DAO;
    }

    private static class WildeberriesProductDAOHolder{
        public static final WildeberriesProductDAO WILDEBERRIES_PRODUCT_DAO = new WildeberriesProductDAO();
    }
}
