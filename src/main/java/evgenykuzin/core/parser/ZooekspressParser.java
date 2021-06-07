package evgenykuzin.core.parser;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FTPManager;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZooekspressParser implements SupplierParser, Loggable {
    private static final int STOCK_CONST = 3;
    //private static final String ORDER_STOCK_COL_NAME = "Заказ, шт.";
    //private static final String mainFileName = "zooekspress";
    //private static final File mainFile = FTPManager.getFileFromSuppliers(mainFileName, ".xls");

    @Override
    public SUPPLIER_NAME getName() {
        return SUPPLIER_NAME.Zooekspress;
    }

    @Override
    public List<SupplierProduct> parseSupplierProducts(List<Product> products) {
        var supProducts = parseProducts();
        var resultProducts = new ArrayList<SupplierProduct>();
        if (supProducts.isEmpty()) return resultProducts;
        for (Product product : products) {
            if (product.getArticle() == null) continue;
            boolean exists = false;
            SupplierProduct productToAdd = null;
            for (SupplierProduct supProduct : supProducts) {
                if (supProduct.getArticle() == null) continue;
                if (supProduct.getArticle().equals(product.getArticle())) {
                    if (supProduct.getBrandName().equalsIgnoreCase(product.getBrandName())) {
                        productToAdd = supProduct;
                        productToAdd.setStock(STOCK_CONST);
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
        }
        return resultProducts;
    }

    @Override
    public List<SupplierProduct> parseProducts() {
        File file = FTPManager.getFileFromSuppliers("zooekspress", ".xls");
        var dataManager = new XlsxDataManagerImpl(file);
        return dataManager.parseProductsList(row -> {
                    var idStr = row.get(TableConfig.ZooekspressConfig.ID_COL_NAME);
                    var id = idStr == null ? null : (long) Double.parseDouble(idStr);
                    var priceStr = row.get(TableConfig.ZooekspressConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.ZooekspressConfig.NAME_COL_NAME);
                    var article = row.get(TableConfig.ZooekspressConfig.ARTICLE_COL_NAME);
                    String brand = row.get(TableConfig.ZooekspressConfig.BRAND_COL_NAME);
                    Integer stock = STOCK_CONST;
                    Integer packageStock = Double.valueOf(row.get(TableConfig.ZooekspressConfig.PACKAGE_STOCK)).intValue();
                    var product = new SupplierProduct();
                    product.setName(name);
                    product.setArticle(article);
                    product.setBrandName(brand);
                    product.setBarcode(null);
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setSupplierName(getName());
                    return product;
                }
        );
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        if (products.isEmpty()) {
            log("orders is empty");
            return false;
        }
        var orderFile = FTPManager.getFileFromSuppliers("zooekspress", ".xls");
        Workbook wb;
        if (orderFile == null) return false;
        try (var inp = new FileInputStream(orderFile)) {
            wb = WorkbookFactory.create(inp);
            try {
                if (wb == null) {
                    log("Failed to write to xls file");
                    return false;
                }
                Sheet sheet = wb.getSheetAt(0);
                for (var product : products) {
                    for (int k = 1; k < sheet.getLastRowNum(); k++) {
                        var row = sheet.getRow(k);
                        if (row == null) continue;
                        Cell articleCell = row.getCell(0);
                        if (product == null || articleCell == null) continue;
                        if (articleCell.toString().equals(product.getArticle())) {
                            Cell orderCell = row.getCell(7);
                            if (orderCell == null) {
                                orderCell = row.createCell(7);
                            }
                            String currOrderStr = orderCell.toString();
                            if (!currOrderStr.isEmpty()) {
                                double currOrder = Integer.parseInt(currOrderStr);
                                double incOrder = currOrder + 1.0;
                                orderCell.setCellValue(incOrder);
                            } else orderCell.setCellValue(1.0);
                        }
                    }
                }
                try (OutputStream fileOut = new FileOutputStream(orderFile)) {
                    wb.write(fileOut);
                    log("zooekspress order was generated");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (wb != null) {
                    wb.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public File getOrderFile() {
        return FTPManager.getFileFromSuppliers("zooekspress_order", ".xls");
    }

    private static class XlsxDataManagerImpl extends XlsxDataManager {
        private static final String keyColName = TableConfig.ZooekspressConfig.ARTICLE_COL_NAME;
        private final File inFile;

        public XlsxDataManagerImpl(File inFile) {
            super(inFile, keyColName);
            this.inFile = inFile;
        }

        @Override
        public synchronized Table parseTable() {
            List<List<Object>> data = new ArrayList<>();
            try (var inp = new FileInputStream(inFile)) {
                var wb = WorkbookFactory.create(inp);
                Sheet sheet = wb.getSheetAt(0);
                int rowsCount = sheet.getLastRowNum();
                String lastBrandName = "LAST_BRAND_NAME";
                for (int i = 0; i <= rowsCount; i++) {
                    List<Object> rowList = new ArrayList<>();
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        int colCounts = row.getLastCellNum();
                        for (int j = 0; j < colCounts; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                String cellValue;
                                CellType cellType = cell.getCellType();
                                var color = cell.getCellStyle().getFillForegroundColor();
                                switch (cellType) {
                                    case STRING: {
                                        cellValue = cell.getStringCellValue();
                                        break;
                                    }
                                    case NUMERIC: {
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        break;
                                    }
                                    default:
                                        cellValue = cell.toString();
                                }
                                if (!cellValue.isEmpty() && color == 51) {
                                    lastBrandName = cellValue;
                                } else if (color == 64) {
                                    rowList.add(cellValue);
                                }
                            }
                        }
                        rowList.add(lastBrandName);
                    }
                    data.add(rowList);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return Table.getEmptyTable();
            }
            var keys = getKeys(data);
            keys.remove("");
            keys.remove("");
            keys.remove("LAST_BRAND_NAME");
            keys.add(TableConfig.ZooekspressConfig.BRAND_COL_NAME);
            return new Table(keyColName, keys, data);
        }

        @Override
        public void removeGarbageFromData(List<List<Object>> data) {
            var temp = new ArrayList<>(data);
            boolean breakFlag = false;
            for (var row : temp) {
                if (row.size() > 0) {
                    if (!breakFlag && !row.get(0).equals(TableConfig.ZooekspressConfig.ARTICLE_COL_NAME)) {
                        data.remove(row);
                    } else {
                        breakFlag = true;
                    }
                }
            }
            for (var row : temp) {
                if (row.isEmpty()) {
                    data.remove(row);
                }
            }
        }
    }

    public static void main(String[] args) {
        var products = ProductDAO.getInstance().getAll();
        var z = new ZooekspressParser();
        var p = z.sendOrders(products);
        System.out.println("p = " + z.getOrderFile());
    }
}
