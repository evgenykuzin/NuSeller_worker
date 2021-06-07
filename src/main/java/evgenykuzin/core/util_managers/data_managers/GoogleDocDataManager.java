package evgenykuzin.core.util_managers.data_managers;

import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public abstract class GoogleDocDataManager implements DataManager, Loggable {
    private static final String APPLICATION_NAME = "Parser";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final int GID = 0;
    public static final String userId = "user3";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private Sheets service;
    private String majorDimension;
    private String range;
    private final String credentialsFilePath;
    private final Comparator<Table.Row> comparator;
    private final String spreadsheetId;
    private final String tableIdColName;
    private final Consumer<Table.Row> beforeRowsConsumer;
    private final Consumer<Table.Row> afterRowsConsumer;

    public GoogleDocDataManager(String credentialsFilePath, String spreadsheetId, String tableIdColName, Comparator<Table.Row> comparator, Consumer<Table.Row> beforeRowsConsumer, Consumer<Table.Row> afterRowsConsumer) {
        this.credentialsFilePath = credentialsFilePath;
        this.spreadsheetId = spreadsheetId;
        this.comparator = comparator;
        this.tableIdColName = tableIdColName;
        this.beforeRowsConsumer = beforeRowsConsumer;
        this.afterRowsConsumer = afterRowsConsumer;
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            range = getAllRange();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public GoogleDocDataManager(String credentialsFilePath, String spreadsheetId, String tableIdColName) {
        this(credentialsFilePath, spreadsheetId, tableIdColName, null, row -> {}, row -> {});
    }

        /**
         * Creates an authorized Credential object.
         *
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
    private  Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(FileManager.getFromResources(credentialsFilePath));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver
                .Builder()
                .setPort(8888)
                .build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(userId);
    }

    private ValueRange getValueRange(String range) throws IOException {
        return service.spreadsheets()
                .values()
                .get(spreadsheetId, range)
                .execute();
    }

    private String getAllRange(int size) throws IOException {
        return String.format("A1:AE%d", size);
    }

    private String getAllRange() throws IOException {
        var r = service.spreadsheets()
                .get(spreadsheetId)
                .execute()
                .getSheets().get(0)
                .getProperties()
                .getGridProperties()
                .getRowCount();
        return getAllRange(r*2);
    }

    private int getNextIndex(List<List<Object>> values) {
        return values.stream()
                .filter(objects -> !objects.isEmpty())
                .filter(objects -> objects.get(0) != null && !objects.get(0).toString().isEmpty())
                .map(objects -> Integer.parseInt(objects.get(0).toString()))
                .max(Integer::compare)
                .orElseGet(() -> new Random().nextInt((int) (values.size()*1.5)));
    }

    private void putIfEmpty(Table.Row row, String key, String value) {
        if (row.get(key) == null || row.get(key).isEmpty()) {
            System.out.println("row = " + row);
            System.out.println("value = " + value);
            row.put(key, value);
        }
    }

    @Override
    public Table parseTable() {
        ValueRange response = null;
        try {
            response = getValueRange(range);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) return Table.getEmptyTable();
        majorDimension = response.getMajorDimension();
        List<List<Object>> values = response.getValues();
        var keys = getKeys(values);
        return new Table(
                tableIdColName,
                keys,
                values,
                beforeRowsConsumer,
                afterRowsConsumer
        );
    }

    @Override
    public void writeAll(Table table) {
        try {
            ValueRange valueRange = new ValueRange();
            var range = getAllRange(table.size()+5);
            valueRange.setRange(range);
            valueRange.setMajorDimension(majorDimension);
            List<List<Object>> values;
            if (comparator != null) {
                values = table.getValuesMatrix(comparator);
            } else {
                values = table.getValuesMatrix();
            }
            valueRange.setValues(values);
            service.spreadsheets().values()
                    .update(spreadsheetId, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getKeys(List<List<Object>> data) {
        return defaultGetKeys(data);
    }

    public abstract List<Product> parseProducts();

}