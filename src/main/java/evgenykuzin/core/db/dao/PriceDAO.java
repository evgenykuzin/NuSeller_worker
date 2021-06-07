package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.Price;

public class PriceDAO extends AbstractDAO<Price, Long> {
    private PriceDAO() {
        super(Price.class);
    }

    @Override
    public String getTableName() {
        return "prices";
    }

    public static PriceDAO getInstance() {
        return PriceDAOHolder.PRICE_DAO;
    }

    private static class PriceDAOHolder{
        public static final PriceDAO PRICE_DAO = new PriceDAO();
    }
}
