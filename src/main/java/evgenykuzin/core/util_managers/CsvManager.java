package evgenykuzin.core.util_managers;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CsvManager {

    public static void writeAll(Collection<Map<String, String>> maps, String[] colNames, String filename) {
        var collection = new ArrayList<String[]>();
        collection.add(colNames);
        collection.addAll(maps.stream()
                .map(map -> map.values().toArray(String[]::new))
                .collect(Collectors.toList())
        );
        try(ICSVWriter writer = openWriter(filename)) {
            writer.writeAll(collection);
        } catch (FileNotFoundException ignored) {
            log("waiting until user close exel");
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private static void log(String msg) {
        LogConfig.logger.log(msg);
    }

    public static List<String[]> read(String filename, String charset, char separator) {
        CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filename), charset))
                .withCSVParser(csvParser)
                .build()) {
            return reader.readAll();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return Collections.emptyList();
    }

    public static List<String[]> read(String filename, String charset) {
        return read(filename, charset, ';');
    }

    public static List<String[]> read(String filename) {
        return read(filename, "UTF-8");
    }

    public static ICSVWriter openWriter(String filename) throws IOException {
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            OutputStream os = new FileOutputStream(filename);
            os.write(239);
            os.write(187);
            os.write(191);
            return new CSVWriterBuilder(new OutputStreamWriter(os, StandardCharsets.UTF_8))
                    .withParser(csvParser)
                    .build();
    }

}
