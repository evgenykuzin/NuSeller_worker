package evgenykuzin.core.parser;

import java.util.Collection;
import java.util.List;

public class SupplierParsersCollectionFactory {
    private static Collection<SupplierParser> supplierParsers;

    public static Collection<SupplierParser> getSupplierParsers() {
        if (supplierParsers == null) {
            supplierParsers = List.of(
                    new ZooekspressParser(),
                    new XmarketParser()
            );
        }
        return supplierParsers;
    }

}
