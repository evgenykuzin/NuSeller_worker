package evgenykuzin.core.util.http.body_builders;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface BodyBuilder {
    BodyBuilder NO_BODY = () -> "";
    String getJsonString();

    default JsonObject getJsonObject() {
        return new Gson().fromJson(getJsonString(), JsonObject.class);
    }
}
