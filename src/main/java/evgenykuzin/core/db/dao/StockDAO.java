package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.Stock;

public class StockDAO extends AbstractDAO<Stock, Long> {
    private static final String PRODUCT_ID_COL_NAME = "productId";

    private StockDAO() {
        super(Stock.class);
    }

    @Override
    public String getTableName() {
        return "stocks";
    }

    private Stock getByMPId(String mpIdColName, Long id) {
        return searchBy(new SearchEntry(mpIdColName, String.valueOf(id))).stream()
                .findFirst()
                .orElse(null);
    }

    public Stock getByProductId(Long id) {
        return getByMPId(PRODUCT_ID_COL_NAME, id);
    }

    public static StockDAO getInstance() {
        return StockDAOHolder.STOCK_DAO;
    }

    private static class StockDAOHolder {
        public static final StockDAO STOCK_DAO = new StockDAO();
    }
}
