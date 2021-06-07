package evgenykuzin.core.util_managers.data_managers;

import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.loger.Loggable;
import lombok.AllArgsConstructor;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class XlsxDataManager implements DataManager, Loggable {
    private final File inFile;
    private final File outFile;
    private final String keyColName;

    public XlsxDataManager(File inFile, String keyColName) {
        this.outFile = inFile;
        this.keyColName = keyColName;
        this.inFile = inFile;
    }

    public static XlsxDataManager getDefaultXslsDataManager(File inFile, String keyColName) {
        return new XlsxDataManager(inFile, keyColName) {
            @Override
            public void removeGarbageFromData(List<List<Object>> data) { }
        };
    }

    @Override
    public synchronized Table parseTable() {
        List<List<Object>> data = new ArrayList<>();
        try (var inp = new FileInputStream(inFile)) {
            var wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            int rowsCount = sheet.getLastRowNum();
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
                            switch (cellType) {
                                case STRING: {
                                    cellValue = cell.getStringCellValue();
                                    break;
                                }
                                case NUMERIC: {
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                    break;
                                }
                                default: cellValue = cell.toString();
                            }
                            rowList.add(cellValue);
                        }
                    }
                }
                data.add(rowList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        var keys = getKeys(data);
        return new Table(keyColName, keys, data);
    }

    @Override
    public synchronized void writeAll(Table table) {
        Workbook wb = null;
            var rows = table.getValuesMatrix();
            try (var inp = new FileInputStream(outFile)) {
                wb = WorkbookFactory.create(inp);
            } catch (EmptyFileException efe) {
                wb = new XSSFWorkbook();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (wb == null) {
                log("Failed to write to xls file");
                return;
            }
            Sheet sheet;
            if (wb.getNumberOfSheets() <= 0) {
                sheet = wb.createSheet();
            } else  {
                sheet = wb.getSheetAt(0);
            }
            String name = outFile.getName();
            for (int k = 1; k < rows.size(); k++) {
                List<Object> ardata = rows.get(k);
                Row row = sheet.createRow(k);
                for (int p = 0; p < ardata.size(); p++) {
                    Cell cell = row.createCell((short) p);
                    cell.setCellValue(ardata.get(p).toString());
                }
            }
            try (OutputStream fileOut = new FileOutputStream(outFile)) {
                wb.write(fileOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log(name + " was updated");

    }

    @Override
    public List<String> getKeys(List<List<Object>> data) {
        if (data == null) return new ArrayList<>();
        removeGarbageFromData(data);
        return defaultGetKeys(data);
    }

    public abstract void removeGarbageFromData(List<List<Object>> data);
}
