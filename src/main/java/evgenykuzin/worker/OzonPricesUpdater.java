package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.PriceDAO;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.Price;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManager;
import com.github.evgenykuzin.core.parser.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OzonPricesUpdater {
    public static void main(String[] args) {
        var productsToUpdate = new ArrayList<Product>();
        var ozonManager = OzonManager.getInstance();
        //var productsDAO = ProductDAO.getInstance();
        var priceDAO = PriceDAO.getInstance();
        for (SupplierParser supplierParser : SupplierParsersCollectionFactory.getSupplierParsers()) {
            var prices = priceDAO.getAll();
            var products = SupplierUtils.supplierFilter(prices.stream()
                    .map(price -> {
                        var product = price.getProduct();
                        return product;
                    }).collect(Collectors.toList()), supplierParser.getName());
            var suppliersProducts = supplierParser.parseSupplierProducts(products);
            for (Price price : prices) {
                var product = price.getProduct();
                if (price.getSupplierPrice() == null) {
                    for (var supplierProduct : suppliersProducts) {
                        if (product.getId().equals(supplierProduct.getProductId())) {
                            price.setSupplierPrice(supplierProduct.getPrice());
                            priceDAO.update(price);
                            break;
                        }
                    }
                }
                var fitnessPrice = price.computeFitnessPrice();
                product.setPrice(fitnessPrice);
                ProductDAO.getInstance().update(product);
                System.out.println("-----------------------------------");
                System.out.println("product = " + product);
                System.out.println("fitnessPrice = " + fitnessPrice);
                productsToUpdate.add(product);
            }

            var res = ozonManager.updatePrices(productsToUpdate);
            System.out.println("res = " + res);
        }
     }
}
