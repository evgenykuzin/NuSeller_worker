package evgenykuzin.core.entities;

import com.github.evgenykuzin.core.db.dao.StockDAO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "stocks")
public class Stock {
    private static final long HOUR = 1000*60*60;
    private static final long HOURS_FOR_FIRST_ORDER = HOUR * 20;
    private static final long EXTRA_HOURS_FOR_ONE_ORDER = HOUR *3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    //@JoinColumn(table = "products", name = "id")
    @Column(name = "product_id")
    Long productId;

    @Column(name = "positive_count", nullable = false, columnDefinition = "int default 0")
    Integer positiveCount;

    @Column(name = "negative_count", nullable = false, columnDefinition = "int default 0")
    Integer negativeCount;

    @Column(name = "to_remove_negative_timestamp")
    Timestamp toRemoveNegativeTimestamp;

    public Integer computeStock() {
        updateNegatives();
        if (negativeCount >= positiveCount) return 0;
        return positiveCount-negativeCount;
    }



    public void addNegative(int count){
        updateNegatives();
        if (negativeCount <= 0) {
            negativeCount = count;
            toRemoveNegativeTimestamp = new Timestamp(new Date().getTime()+HOURS_FOR_FIRST_ORDER);
        } else {
            negativeCount += count;
            long current = 0L;
            if (toRemoveNegativeTimestamp != null) current = toRemoveNegativeTimestamp.getTime();
            toRemoveNegativeTimestamp = new Timestamp(current+EXTRA_HOURS_FOR_ONE_ORDER);
        }
    }

    public void updateNegatives() {
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        if (toRemoveNegativeTimestamp != null) {
            if (currentTimestamp.compareTo(toRemoveNegativeTimestamp) >= 0) {
                negativeCount = 0;
                toRemoveNegativeTimestamp = null;
            }
        }
    }

    public static Stock defaultStock(Long productId, int positiveCount) {
        Stock stock = getStock(productId);
        stock.updateNegatives();
        stock.setPositiveCount(positiveCount);
        return stock;
    }

    public static Stock tempStock(int positiveCount) {
        return new Stock(null, null, positiveCount, 0, null);
    }

    public static Stock getStock(Long productId) {
        return StockDAO.getInstance().getByProductId(productId);
    }


}
