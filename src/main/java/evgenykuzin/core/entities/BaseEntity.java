package evgenykuzin.core.entities;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class BaseEntity<I extends Serializable> {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    protected I id;
}
