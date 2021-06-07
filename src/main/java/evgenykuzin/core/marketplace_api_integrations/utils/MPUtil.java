package evgenykuzin.core.marketplace_api_integrations.utils;

import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.body_builders.JsonBodyBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;

public class MPUtil {
    public static JsonObject getJsonProp(JsonObject jsonObject, String key) {
        return jsonObject.getAsJsonObject(key);
    }

    public static String getStrProp(JsonObject jsonObject, String key) {
        var jsonV = jsonObject.get(key);
        if (jsonV == null) return null;
        return jsonV.getAsString();
    }

    public static Integer getIntProp(JsonObject jsonObject, String key) {
        var jsonV = jsonObject.get(key);
        if (jsonV == null) return null;
        return jsonV.getAsInt();
    }

    public static Double getDoubleProp(JsonObject jsonObject, String key) {
        var jsonV = jsonObject.get(key);
        if (jsonV == null) return null;
        return jsonV.getAsDouble();
    }

    public static Long getLongProp(JsonObject jsonObject, String key) {
        var jsonV = jsonObject.get(key);
        if (jsonV == null) return null;
        return jsonV.getAsLong();
    }

    public static JsonObject safeAddProperty(JsonObject jsonObject, String key, Object value) {
        if (key != null && !key.isEmpty() && value != null) {
            jsonObject.addProperty(key, String.valueOf(value));
        }
        return jsonObject;
    }

    @AllArgsConstructor
    public static class JsonBuilder {
        private final JsonObject jsonObject;

        public JsonBuilder() {
            this.jsonObject = new JsonObject();
        }

        public JsonBuilder(String string) {
            this.jsonObject = new JsonParser().parse(string).getAsJsonObject();
        }

        public JsonBuilder addProperty(String k, String v) {
            jsonObject.addProperty(k, v);
            return this;
        }

        public JsonBuilder addIntProperty(String k, Integer v) {
            jsonObject.addProperty(k, v);
            return this;
        }

        public JsonBuilder addPropertyInObj(String k, String v, String in) {
            jsonObject.get(in).getAsJsonObject().addProperty(k, v);
            return this;
        }

        public JsonBuilder addPropertyInArr(String v, String in) {
            jsonObject.get(in).getAsJsonArray().add(v);
            return this;
        }

        public JsonBuilder addInArr(JsonObject jo, String in) {
            jsonObject.getAsJsonArray(in)
                    .add(jo);
            return this;
        }

        public JsonBuilder addInArr(JsonBuilder jsonBuilder, String in) {
            return addInArr(jsonBuilder
                    .build()
                    .getJsonObject(), in);
        }

        public JsonBuilder addObjInObj(String key, JsonObject jo, String in) {
            jsonObject.getAsJsonObject(in).add(key, jo);
            return this;
        }

        public JsonBuilder addArrInObj(String key, JsonArray jsonArray, String in) {
            jsonObject
                    .getAsJsonObject(in)
                    .add(key, jsonArray);
            return this;
        }

        public JsonBuilder add(String k, JsonElement v) {
            jsonObject.add(k, v);
            return this;
        }

        public JsonBuilder addNewObj(String k) {
            return add(k, new JsonObject());
        }

        public JsonBuilder addNewArr(String k) {
            return add(k, new JsonArray());
        }

        public BodyBuilder build() {
            return (JsonBodyBuilder) object -> new JsonBodyBuilder.BodyJson(jsonObject);
        }

        @Override
        public String toString() {
            return jsonObject.toString();
        }
    }
}
