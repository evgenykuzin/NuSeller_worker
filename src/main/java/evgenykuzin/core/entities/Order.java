package evgenykuzin.core.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "supplier_enum")
    String supplierEnum;

    @Column(name = "marketplace_enum")
    String marketplaceEnum;

    @Column(name = "count")
    Integer count;

    @Column(name = "timestamp")
    Timestamp timestamp;


}
