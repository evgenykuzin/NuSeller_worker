package evgenykuzin.core.util.http.body_builders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface JsonBodyBuilder extends BodyBuilder {
    Body<JsonObject> construct(JsonObject object);
    default Body<JsonObject> construct() {
        JsonObject object = new JsonObject();
        return construct(object);
    }

    default Map<String, String> getBodyMap() {
        Set<Map.Entry<String, JsonElement>> entrySet = construct().getObject().entrySet();
        Map<String, String> stringMap = new HashMap<>();
        entrySet.forEach(entry -> {
            String value;
            JsonElement element = entry.getValue();
            if (element.isJsonArray()) {
                value = element.getAsJsonArray().getAsString();
            } else if (element.isJsonObject()) {
                value = element.getAsJsonObject().toString();
            } else if (element.isJsonPrimitive()) {
                value = element.getAsJsonPrimitive().getAsString();
            } else {
                value = element.getAsJsonNull().getAsString();
            }
            stringMap.put(entry.getKey(), value);
        });
        return stringMap;
    }

    default String getJsonString() {
        return new Gson().toJson(construct().getObject());
    }

    @AllArgsConstructor
    @Getter
    class BodyJson implements Body<JsonObject> {
        private final JsonObject object;
    }
}
