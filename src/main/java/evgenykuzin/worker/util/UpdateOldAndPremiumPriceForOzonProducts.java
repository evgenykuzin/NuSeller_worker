package evgenykuzin.worker.util;

import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManager;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class UpdateOldAndPremiumPriceForOzonProducts {
    public static void main(String[] args) {
        var ozonManager = OzonManager.getInstance();
        var products = ozonManager.getProductsFromMP();
        var queue = new ArrayBlockingQueue<Product>(products.size());
        queue.addAll(products);
        while (!queue.isEmpty()) {
            var limitedProducts = new ArrayList<Product>();
            for (int i = 0; i < 999; i++) {
                limitedProducts.add(queue.poll());
            }
            var resp = ozonManager.updatePrices(limitedProducts);
            System.out.println("resp = " + resp);
        }
    }
}
