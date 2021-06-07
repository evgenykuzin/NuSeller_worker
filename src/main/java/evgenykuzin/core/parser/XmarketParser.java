package evgenykuzin.core.parser;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.util_managers.FTPManager;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.core.util_managers.data_managers.CsvDataManager;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManagerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmarketParser implements SupplierParser {
    private static final String URL = "https://www.xmarket.ru/";

    @Override
    public SUPPLIER_NAME getName() {
        return SUPPLIER_NAME.Xmarket;
    }

    public List<SupplierProduct> parseSupplierProducts(List<Product> products) {
        var supProducts = parseProducts();
        var resultProducts = new ArrayList<SupplierProduct>();
        if (supProducts.isEmpty()) return resultProducts;
        products.forEach(product -> {
            SupplierProduct productToAdd = null;
            boolean exists = false;
            for (var supProduct : supProducts) {
                if (supProduct.getArticle().equals(product.getArticle())) {
                    if (supProduct.getBrandName().equals(product.getBrandName())) {
                        productToAdd = supProduct;
                        productToAdd.setProductId(product.getId());
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                productToAdd = new SupplierProduct();
                productToAdd.setProductId(product.getId());
                productToAdd.setArticle(product.getArticle());
                productToAdd.setBarcode(product.getBarcode());
                productToAdd.setName(product.getName());
                productToAdd.setBrandName(product.getBrandName());
                productToAdd.setSupplierName(product.getSupplierName());
                productToAdd.setStock(0);
            }
            resultProducts.add(productToAdd);
        });
        return resultProducts;
    }

    @Override
    public List<SupplierProduct> parseProducts() {
        return getCsvDataManager().parseProductsList(row -> {
            var idStr = row.get(TableConfig.XmarketConfig.ID_COL_NAME);
            var id = idStr == null ? null : Long.parseLong(idStr);
            var priceStr = row.get(TableConfig.XmarketConfig.PRICE_COL_NAME);
            var price = priceStr == null ? null : Double.parseDouble(priceStr);
            var name = row.get(TableConfig.XmarketConfig.NAME_COL_NAME);
            var brand = row.get(TableConfig.XmarketConfig.BRAND_COL_NAME);
            var barcode = row.get(TableConfig.XmarketConfig.BARCODE_COL_NAME);
            var article = row.get(TableConfig.XmarketConfig.ARTICLE_COL_NAME);
            var stockStr = row.get(TableConfig.XmarketConfig.STOCKS_COL_NAME);
            var stock = Integer.parseInt(stockStr);
            var product = new SupplierProduct();
            product.setName(name);
            product.setArticle(article);
            product.setBrandName(brand);
            product.setBarcode(barcode);
            product.setPrice(price);
            product.setStock(stock);
            product.setSupplierName(getName());
            return product;
        });
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        return false;
    }

    private static CsvDataManager getCsvDataManager() {
        File file = null;
        try {
            file = FileManager.download(
                    URL + "upload/clients/trade.csv",
                    FTPManager.getFileFromSuppliers("trade", ".csv")
            );
            FTPManager.uploadFileToSuppliers(file, "trade", ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DataManagerFactory.getCsvManager(file, TableConfig.XmarketConfig.ARTICLE_COL_NAME);
    }

}
