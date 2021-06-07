package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.product.OzonProduct;

public class OzonProductDAO extends AbstractDAO<OzonProduct, String> {
    private OzonProductDAO() {
        super(OzonProduct.class);
    }

    @Override
    public String getTableName() {
        return "ozon_products_extra";
    }

    public static OzonProductDAO getInstance() {
        return OzonProductDAOHolder.OZON_PRODUCT_DAO;
    }

    private static class OzonProductDAOHolder{
        public static final OzonProductDAO OZON_PRODUCT_DAO = new OzonProductDAO();
    }
}
