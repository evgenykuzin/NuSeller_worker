package evgenykuzin.core.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "dimensions")
public class Dimensions {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "dim_id")
    Long dimId;

    Double length;

    Double width;

    Double height;

    Double weight;

    //@JoinColumn(table = "products", name = "id")
    @Column(name = "product_id")
    Long productId;

//    public void setId(Long id) {
//        super.setId(id);
//    }
//
//    @Id
//    //@GeneratedValue(strategy = GenerationType.IDENTITY)
//    public Long getId() {
//        return super.getId();
//    }
}
