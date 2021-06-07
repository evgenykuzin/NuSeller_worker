package evgenykuzin.core.util_managers;

import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.services.HttpService;
import com.github.evgenykuzin.core.util.http.services.NetHttpService;
import com.google.gson.JsonParser;

public class AntiCaptchaManager {
    private static final String clientKey = "53c05b90303e57b65fd6251cfe3c3081";

    public static String createTask(String gReCaptchaKey, String url) {
        NetHttpService httpService = new NetHttpService();
        var request = httpService.constructRequest("http://api.anti-captcha.com/createTask",
                HttpService.POST,
                new HeadersModelImpl(),
                () -> String.format("{\n" +
                        "    \"clientKey\":\"%s\",\n" +
                        "    \"task\":\n" +
                        "        {\n" +
                        "            \"type\":\"RecaptchaV2TaskProxyless\",\n" +
                        "            \"websiteURL\":\"%s\",\n" +
                        "            \"websiteKey\":\"%s\"\n" +
                        "        }\n" +
                        "}", clientKey, url, gReCaptchaKey));
        var response = httpService.getResponse(request);
        var json = new JsonParser().parse(response.getResponseString()).getAsJsonObject();
        return json.get("taskId").getAsString();
    }

    public static String getResult(String taskId) {
        NetHttpService httpService = new NetHttpService();
        var request = httpService.constructRequest("http://api.anti-captcha.com/getTaskResult",
                HttpService.POST,
                new HeadersModelImpl(),
                () -> String.format("{\n" +
                        "    \"clientKey\":\"%s\",\n" +
                        "    \"taskId\": %s\n" +
                        "}", clientKey, taskId));
        var response = httpService.getResponse(request);
        System.out.println("response = " + response);
        if (!response.getResponseString().contains("solution")) return null;
        var solutionHash = new JsonParser()
                .parse(response.getResponseString())
                .getAsJsonObject()
                .get("solution")
                .getAsJsonObject()
                .get("gRecaptchaResponse")
                .getAsString();
        System.out.println("solutionHash = " + solutionHash);
        return solutionHash;
    }

}
