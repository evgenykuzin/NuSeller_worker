package evgenykuzin.core.entities.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.persistence.Table;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ozon_products_extra")
public class OzonProduct {
//    @Transient
//    String name;
//
//    @Transient
//    String brandName;
//
//    @Transient
//    String article;
//
//    @Transient
//    String barcode;
//
//    @Transient
//    Double price;
//
//    @Transient
//    Stock stock;
//
//    @Transient
//    String supplierName;
//
//    @Transient
//    Dimensions dimensions;
//
//    @Transient
//    List<String> urls;

    @Id
    @Column(name = "ozon_id", nullable = false)
    String ozonId;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "category_id")
    Long categoryId;

    @Column(name = "sku_fbs")
    String skuFbs;

    @Column(name = "sku_fbo")
    String skuFbo;

}
