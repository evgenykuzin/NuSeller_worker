package evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.PriceDAO;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.OzonProduct;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManagerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleDocViewManager implements Runnable {
    public static void main(String[] args) {
        updateView();
    }

    @Override
    public void run() {
        updateView();
    }

    public static void updateView() {
        PriceDAO priceDAO = PriceDAO.getInstance();
        var dataManager = DataManagerFactory.getOzonGoogleDocDataManagerForView(FIELDS.id.name());
        List<Object> keys = Arrays.stream(FIELDS.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        Table table = new Table(FIELDS.id.name(), keys);
        priceDAO.getAll().forEach(price -> {
            Product product = price.getProduct();
            OzonProduct ozonProduct = product.getOzonProduct();
            String priceId = String.valueOf(price.getId());
            var row = table.get(priceId);
            if (row == null) row = new Table.Row();
            row.put(FIELDS.id.name(), String.valueOf(priceId));
            row.put(FIELDS.product_id.name(), String.valueOf(product.getId()));
            row.put(FIELDS.price.name(), String.valueOf(price.getPrice()));
            row.put(FIELDS.concurrent_price.name(), String.valueOf(price.getConcurrentPrice()));
            row.put(FIELDS.diff_of_prices.name(), String.valueOf(price.getPricesDiff()));
            row.put(FIELDS.supplier_price.name(), String.valueOf(price.getSupplierPrice()));
            row.put(FIELDS.concurrent_url.name(), String.valueOf(price.getConcurrentUrl()));
            row.put(FIELDS.article.name(), product.getArticle());
            row.put(FIELDS.name.name(), product.getName());
            row.put(FIELDS.brand_name.name(), product.getBrandName());
            row.put(FIELDS.barcode.name(), product.getBarcode());
            row.put(FIELDS.stock.name(), String.valueOf(product.getStock().computeStock()));
            row.put(FIELDS.ozon_id.name(), ozonProduct.getOzonId());
            row.put(FIELDS.category_id.name(), String.valueOf(ozonProduct.getCategoryId()));
            row.put(FIELDS.sku_fbs.name(), ozonProduct.getSkuFbs());
            row.put(FIELDS.sku_fbo.name(), ozonProduct.getSkuFbo());
            System.out.println("row = " + row);
            table.put(priceId, row);
        });
        dataManager.writeAll(table);
    }

    private enum FIELDS {
        id,
        product_id,
        price,
        concurrent_price,
        diff_of_prices,
        supplier_price,
        concurrent_url,
        article,
        name,
        brand_name,
        barcode,
        stock,
        ozon_id,
        category_id,
        sku_fbs,
        sku_fbo
    }
}
