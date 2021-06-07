package evgenykuzin.core.util_managers.data_managers;

import com.github.evgenykuzin.core.entities.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface DataManager {

    Table parseTable();

    void writeAll(Table table);

    List<String> getKeys(List<List<Object>> data);

    default <P> List<P> parseProductsList(Function<Table.Row, P> mapFunction) {
        return parseTable().values().stream()
                .map(mapFunction)
                .collect(Collectors.toList());
    }

    default List<String> defaultGetKeys(List<List<Object>> data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();
        var keys = data.get(0);
        data.remove(keys);
        return keys.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
