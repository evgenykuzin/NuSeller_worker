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
@Table(name = "yamarket_products_extra")
public class YamarketProduct {
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
    @Column(name = "yamarket_id", nullable = false)
    String yamarketId;

    //@OneToOne(targetEntity = Product.class, fetch = FetchType.LAZY)
    //@JoinColumn(table = "products", name = "id")
    //Product product;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "category")
    String category;

    @Column(name = "package_stock")
    Integer packageStock;

    @Column(name = "description_text")
    String descriptionText;

}
