package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.DAO;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.parser.SUPPLIER_NAME;
import com.github.evgenykuzin.core.parser.SupplierParsersCollectionFactory;
import com.github.evgenykuzin.core.util.loger.Loggable;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

@AllArgsConstructor
public abstract class ProductsUpdaterFromMP implements Loggable {
    private final MPManager<Product> mpManager;
    private final ProductDAO productDAO;

    public abstract void additionalDbUpdates(Product updatedProduct, Product otherProduct);

    public void doUpdates() {
        var products = mpManager.getProductsFromMP();
        updateDatabase(products);
        //updateGoogleDoc(products);
    }

    public void updateDatabase(Collection<Product> products) {
        try {
            var existedInDBAndMP = new HashSet<Product>();
            products.forEach(product -> {
                Long id = null;
                if (mpManager.getName().equals(MP_NAME.OZON)) {
                    var ozonProduct = product.getOzonProduct();
                    if (ozonProduct != null) {
                        id = ozonProduct.getProductId();
                    }
                } else if (mpManager.getName().equals(MP_NAME.YAMARKET)) {
                    var yamarketProduct = product.getYamarketProduct();
                    if (yamarketProduct != null) {
                        id = yamarketProduct.getProductId();
                    }
                } else if (mpManager.getName().equals(MP_NAME.WILDEBERRIES)) {
                    var wildeberriesProduct = product.getWildeberriesProduct();
                    if (wildeberriesProduct != null) {
                        id = wildeberriesProduct.getProductId();
                    }
                }
                Product dbProduct;
                if (id != null) {
                    dbProduct = productDAO.get(id);
                } else {
                    dbProduct = productDAO.getByArticleAndBrand(product.getArticle(), product.getBrandName());
                }
                if (dbProduct == null) {
                    dbProduct = productDAO.searchBy(
                            new DAO.SearchEntry("barcode", product.getBarcode()),
                            new DAO.SearchEntry("article", product.getArticle())
                    ).get(0);
                }
                if (dbProduct != null) {
                    if (product.getBarcode() != null) {
                        dbProduct.setBarcode(product.getBarcode());
                    }
                    if (product.getName() != null) {
                        dbProduct.setName(product.getName());
                    }
                    if (product.getStock() != null) {
                        dbProduct.setStock(product.getStock());
                    }
                    if (product.getUrls() != null) {
                        dbProduct.setUrls(product.getUrls());
                    }
                    if (dbProduct.getBrandName() == null || dbProduct.getBrandName().isEmpty()) {
                        var brandName = findBrandName(dbProduct);
                        if (brandName != null) dbProduct.setBrandName(brandName);
                    }
                    if (dbProduct.getSupplierName() == null) {
                        var supplierName = findSupplierName(dbProduct);
                        if (supplierName != null) dbProduct.setSupplierName(supplierName);
                    }
                    additionalDbUpdates(dbProduct, product);
                    existedInDBAndMP.add(dbProduct);
                    productDAO.update(dbProduct);
                } else {
                    log("importing new product in db " + product + " from ozon");
                    productDAO.save(product);
                }

            });

            var dbProducts = productDAO.getAll();
            for (var dbProduct : dbProducts) {
                if (!existedInDBAndMP.contains(dbProduct)) {
                    productDAO.delete(dbProduct);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected <T> T findAttribute(Product product, Function<SupplierProduct, T> function) {
        var supplierParsers = SupplierParsersCollectionFactory.getSupplierParsers();
        for (var supplierParser : supplierParsers) {
            var supplierProducts = supplierParser.parseProducts();
            for (SupplierProduct supplierProduct : supplierProducts) {
                if (product.getArticle().equals(supplierProduct.getArticle())) {
                    return function.apply(supplierProduct);
                }
            }
        }
        return null;
    }

    protected String findBrandName(Product product) {
        return findAttribute(product, SupplierProduct::getBrandName);
    }

    protected SUPPLIER_NAME findSupplierName(Product product) {
        return findAttribute(product, sp -> {
            var supBrand = sp.getBrandName();
            var oznBrand = product.getBrandName();
            if (supBrand != null && supBrand.equals(oznBrand)) {
                return sp.getSupplierName();
            }
            return null;
        });
    }

}
