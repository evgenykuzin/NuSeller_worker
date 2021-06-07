package evgenykuzin.core.marketplace_api_integrations.ozon;

import com.google.gson.JsonObject;

import static com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil.getStrProp;

public class OzonManagerUtil {

    static String getSku(JsonObject jsonObject, String source) {
        String sku = null;
        var jsonArray = jsonObject.getAsJsonArray("sources");
        if (jsonArray == null) return null;
        for (var e : jsonArray) {
            var jObj = e.getAsJsonObject();
            var sourceValue = getStrProp(jObj, "source");
            if (sourceValue != null && sourceValue.equals(source)) {
                sku = getStrProp(jObj, "sku");
            }
        }
        return sku;
    }
}
