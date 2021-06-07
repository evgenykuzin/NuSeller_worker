package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.entities.product.Product;

import java.util.List;

public class ProductDAO extends AbstractDAO<Product, Long> {
    private ProductDAO() {
        super(Product.class);
    }

    @Override
    public String getTableName() {
        return "products";
    }

    public static ProductDAO getInstance() {
        return ProductDAOHolder.PRODUCT_DAO;
    }

    private static class ProductDAOHolder {
        public static final ProductDAO PRODUCT_DAO = new ProductDAO();
    }

    public List<Product> searchByArticleAndName(String article, String name) {
        return searchBy(
                new SearchEntry("article", article),
                new SearchEntry("product_name", name)
        );
    }

    public List<Product> searchByArticleAndBrand(String article, String brandName) {
        return searchBy(
                new SearchEntry("article", article),
                new SearchEntry("brand_name", brandName)
        );
    }

    public Product getByArticleAndBrand(String article, String brandName) {
        var search = searchByArticleAndBrand(article, brandName);
        if (search.isEmpty()) return null;
        else return search.get(0);
    }

    public Product getByArticleAndName(String article, String name) {
        var search = searchByArticleAndName(article, name);
        if (search.isEmpty()) return null;
        else return search.get(0);
    }
}
