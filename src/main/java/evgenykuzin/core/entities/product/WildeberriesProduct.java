package evgenykuzin.core.entities.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "wildeberries_products_extra")
public class WildeberriesProduct {
    @Id
    @Column(name = "wb_id", nullable = false)
    Integer wbId;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "chrt_id")
    String chrtId;

    @Column(name = "category")
    String category;

    @Column(name = "price")
    Double price;

}
