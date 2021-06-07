package evgenykuzin.core.entities;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Getter
public class Table extends LinkedHashMap<String, Table.Row> {
    private final List<String> keys;
    private final String idKeyName;

    public static Table getEmptyTable() {
        return new Table("", Collections.emptyList(), new ArrayList<List<Object>>());
    }

    public Table(String idKeyName, List<String> keys, Collection<List<Object>> collections, Consumer<Row> beforeRowFiller, Consumer<Row> afterRowFiller) {
        this.idKeyName = idKeyName;
        this.keys = keys;
        var rowHashMap = new HashMap<String, Row>();
        collections.stream()
                .map(objects -> {
                    var row = new Row();
                    beforeRowFiller.accept(row);
                    fillBaseRow(row, objects);
                    afterRowFiller.accept(row);
                    return row;
                })
                .filter(row -> !row.isFieldsAreEmpty(idKeyName))
                .forEach(row -> {
                    rowHashMap.put(row.get(idKeyName), row);
                });
        putAll(rowHashMap);
    }

    public Table(String idKeyName, List<String> keys, Collection<List<Object>> collections) {
        this(idKeyName, keys, collections, row -> {
        }, row -> {
        });
    }

    public Table(String idKeyName, List<Object> keys, List<Row> collection) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
        collection.forEach(row -> put(row.get(idKeyName), row));
    }

    public Table(String idKeyName, List<Object> keys) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
    }

    private void fillBaseRow(Row row, List<Object> objects) {
        var objectsCopy = new ArrayList<>(objects);
        int objectsSize = objectsCopy.size();
        if (objectsSize < keys.size()) {
            var s = keys.size() - objectsSize + 2;
            for (int i = 0; i < s; i++) {
                objectsCopy.add("");
            }
        }
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var obj = objectsCopy.get(i);
            var value = obj == null ? null : obj.toString();
            if (key.equals(idKeyName) && (value == null || value.isEmpty())) continue;
            row.put(key, value);
        }
    }

    public List<List<Object>> getValuesMatrix(Comparator<Row> comparator) {
        List<List<Object>> res = new ArrayList<>();
        res.add(keys.stream()
                .map(s -> ((Object) s))
                .collect(Collectors.toList()));
        List<List<Object>> v = values().stream()
                .sorted(comparator)
                .map(row -> row.values()
                        .stream()
                        .map(s -> ((Object) s))
                        .collect(Collectors.toList())
                ).collect(toList());
        res.addAll(v);
        return res;
    }

    public List<List<Object>> getValuesMatrix() {
        return getValuesMatrix((o1, o2) -> 0);
    }

    public void updateRowValue(String id, String keyName, String value) {
        var m = get(id);
        m.put(keyName, value);
        put(id, m);
    }

    public Collection<JsonObject> convertToJsonCollection() {
        return values().stream().map(row -> {
            JsonObject item = new JsonObject();
            for (String key : keys) {
                var rowValue = row.get(key);
                item.addProperty(key, rowValue);
            }
            return item;
        }).collect(Collectors.toList());
    }

    public static class Row extends LinkedHashMap<String, String> {
        public boolean isFieldsAreEmpty(String idKeyName) {
            var vm = new LinkedHashMap<>(this);
            vm.remove(idKeyName);
            var v = vm.values();
            return v.stream().allMatch(s -> s == null || s.isEmpty()) || isEmpty();
        }
    }

    public static class KeysAndValuesNotMatchesSizesException extends Exception {
    }
}
