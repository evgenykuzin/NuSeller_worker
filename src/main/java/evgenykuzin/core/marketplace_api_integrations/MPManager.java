package evgenykuzin.core.marketplace_api_integrations;

import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.*;
import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.services.HttpService;
import com.github.evgenykuzin.core.util.http.util.URLUtils;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface MPManager <P> {
    JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder);

    JsonObject updateProductStocks(Collection<P> products, String supplierName);

    Collection<JsonObject> getProductsJsonsFromMP();

    P constructProduct(JsonObject jsonObject);

    JsonObject constructJsonFromProduct(Product product);

    List<P> getOrderedProducts();

    JsonObject importProductsToMP(Collection<P> products);

    MP_NAME getName();

    default <T> JsonObject executeWithMaxItems(Collection<T> allProducts, int maxItems, Function<Collection<T>, JsonObject> function) {
        JsonBuilder result = new JsonBuilder();
        result.addNewArr("results");
        ArrayBlockingQueue<T> productArrayBlockingQueue = new ArrayBlockingQueue<>(allProducts.size());
        productArrayBlockingQueue.addAll(allProducts);
        while (!productArrayBlockingQueue.isEmpty()) {
            List<T> productsForRequest = new ArrayList<>();
            for (int i = 0; i < maxItems; i++) {
                if (productArrayBlockingQueue.isEmpty()) break;
                productsForRequest.add(productArrayBlockingQueue.poll());
            }
            JsonObject res = function.apply(productsForRequest);
            result.addInArr(res, "results");
        }
        return result.build().getJsonObject();
    }

    default JsonObject executePostRequest(String mapping, JsonBuilder builder) {
        return executeRequest(mapping, HttpService.POST, builder.build());
    }

    default JsonObject executePostRequest(String mapping) {
        return executeRequest(mapping, HttpService.POST, BodyBuilder.NO_BODY);
    }

    default JsonObject executeGetRequest(String mapping) {
        return executeRequest(mapping, HttpService.GET, BodyBuilder.NO_BODY);
    }

    default JsonObject executeGetRequest(String mapping, Map<String, String> params) {
        return executeRequest(String.format("%s?%s", mapping, URLUtils.urlEncodeUTF8(params)), HttpService.GET, BodyBuilder.NO_BODY);
    }

    default Collection<P> getProductsFromMP() {
        return getProductsJsonsFromMP().stream()
                .map(this::constructProduct)
                .collect(Collectors.toList());
    }
}