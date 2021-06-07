package evgenykuzin.core.entities.product;

import com.github.evgenykuzin.core.db.dao.*;
import com.github.evgenykuzin.core.db.util.StringListConverter;
import com.github.evgenykuzin.core.entities.Dimensions;
import com.github.evgenykuzin.core.entities.Stock;
import com.github.evgenykuzin.core.parser.SUPPLIER_NAME;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED)
@ToString(of = {"id", "name", "brandName", "article", "barcode", "supplierName"})
//@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    public void setId(Long id) {
//        super.setId(id);
//    }
//
//    @Id
//    public Long getId() {
//        return super.getId();
//    }

    @Column(name = "product_name")
    String name;

    @Column(name = "brand_name")
    String brandName;

    @Column(name = "article")
    String article;

    @Column(name = "barcode")
    String barcode;

    @Column(name = "price")
    Double price;

    @Column(name = "supplier_name")
    @Enumerated(EnumType.STRING)
    SUPPLIER_NAME supplierName;

    @Convert(converter = StringListConverter.class)
    @Column(name = "urls")
    List<String> urls;

    //@OneToOne(targetEntity = Stock.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JoinColumn(table = "stocks", name = "id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = Stock.class)
    Stock stock;

    //@OneToOne(targetEntity = Dimensions.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JoinColumn(table = "dimensions", name = "id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = Dimensions.class)
    Dimensions dimensions;

    //@OneToOne(targetEntity = YamarketProduct.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JoinColumn(table = "yamarket_products_extra", name = "yamarket_id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    YamarketProduct yamarketProduct;

    //@OneToOne(targetEntity = OzonProduct.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JoinColumn(table = "products_ozon", name = "ozon_id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    OzonProduct ozonProduct;

    @JoinColumn(name = "ozon_id", table = "ozon_products_extra")
    String ozonId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    WildeberriesProduct wildeberriesProduct;

    public void setStock(Stock stock) {
        stock.setProductId(id);
        this.stock = stock;
    }

    public void setDimensions(Dimensions dimensions) {
        dimensions.setProductId(id);
        this.dimensions = dimensions;
    }

    public void setYamarketProduct(YamarketProduct yamarketProduct) {
        yamarketProduct.setProductId(id);
        this.yamarketProduct = yamarketProduct;
    }

    public void setOzonProduct(OzonProduct ozonProduct) {
        ozonProduct.setProductId(id);
        this.ozonProduct = ozonProduct;
    }

    public void setWildeberriesProduct(WildeberriesProduct wildeberriesProduct) {
        wildeberriesProduct.setProductId(id);
        this.wildeberriesProduct = wildeberriesProduct;
    }

    public Stock getStock() {
        if (stock != null && stock.getProductId() == null) {
            stock.setProductId(id);
            StockDAO.getInstance().update(stock);
        }
        return stock;
    }

    public Dimensions getDimensions() {
        if (dimensions != null && dimensions.getProductId() == null) {
            dimensions.setProductId(id);
            DimensionsDAO.getInstance().update(dimensions);
        }
        return dimensions;
    }

    public YamarketProduct getYamarketProduct() {
        if (yamarketProduct != null && yamarketProduct.getProductId() == null) {
            yamarketProduct.setProductId(id);
            YamarketProductDAO.getInstance().update(yamarketProduct);
        }
        return yamarketProduct;
    }

    public OzonProduct getOzonProduct() {
        if (ozonProduct != null && ozonProduct.getProductId() == null) {
            ozonProduct.setProductId(id);
            OzonProductDAO.getInstance().update(ozonProduct);
        }
        return ozonProduct;
    }

    public WildeberriesProduct getWildeberriesProduct() {
        if (wildeberriesProduct != null && wildeberriesProduct.getProductId() == null) {
            wildeberriesProduct.setProductId(id);
            WildeberriesProductDAO.getInstance().update(wildeberriesProduct);
        }
        return wildeberriesProduct;
    }
}
