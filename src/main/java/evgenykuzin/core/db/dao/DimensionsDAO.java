package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.Dimensions;

public class DimensionsDAO extends AbstractDAO<Dimensions, Long> {
    public DimensionsDAO() {
        super(Dimensions.class);
    }
    @Override
    public String getTableName() {
        return "dimensions";
    }

    public static DimensionsDAO getInstance() {
        return DimensionsDAOHolder.DIMENSIONS_DAO;
    }

    private static class DimensionsDAOHolder {
        public static final DimensionsDAO DIMENSIONS_DAO = new DimensionsDAO();
    }
}
