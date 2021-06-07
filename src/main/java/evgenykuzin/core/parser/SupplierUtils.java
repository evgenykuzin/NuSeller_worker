package evgenykuzin.core.parser;

import com.github.evgenykuzin.core.entities.product.Product;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierUtils {
    public static <P extends Product> List<Product> supplierFilter(Collection<P> products, SUPPLIER_NAME supplierName) {
        return products.stream()
                .filter(product -> {
                    var supName = product.getSupplierName();
                    if (supName == null) return false;
                    return supName.equals(supplierName);
                }).collect(Collectors.toList());
    }
}
