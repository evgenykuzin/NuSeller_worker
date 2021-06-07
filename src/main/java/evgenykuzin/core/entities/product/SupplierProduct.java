package evgenykuzin.core.entities.product;

import com.github.evgenykuzin.core.parser.SUPPLIER_NAME;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PROTECTED)
public class SupplierProduct {
    Long productId;
    String article;
    String barcode;
    String brandName;
    String name;
    Double price;
    Integer stock;
    SUPPLIER_NAME supplierName;
}
