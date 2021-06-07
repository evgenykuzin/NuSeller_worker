package evgenykuzin.core.util.http.services;

import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.body_builders.JsonBodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.HeadersModel;
import com.github.evgenykuzin.core.util.http.models.ClientModel;
import com.github.evgenykuzin.core.util.http.models.RequestModel;
import com.github.evgenykuzin.core.util.http.models.RequestModelDefault;
import com.github.evgenykuzin.core.util.http.models.ResponseModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class NetHttpService implements HttpService<HttpRequest, HttpClient> {
    @Override
    public ClientModel<HttpClient> constructClient() {
        return HttpClient::newHttpClient;
    }

    @Override
    public RequestModel<HttpRequest> constructRequest(String url, String method, HeadersModel headers, BodyBuilder bodyBuilder) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        headers.getSet().forEach(header -> builder.setHeader(header.getKey(), header.getValue()));
        HttpRequest request = builder
                .method(method, HttpRequest.BodyPublishers.ofString(bodyBuilder.getJsonString()))

                .timeout(Duration.ofSeconds(3)) //

                .build();
        return new RequestModelDefault<>(
                url,
                request.toString(),
                bodyBuilder.getJsonString(),
                headers,
                request
        );
    }

    public RequestModel<HttpRequest> constructRequest(String url, String method, HeadersModel headersModel, Map<String, String> params) {
        return constructRequest(url, method, headersModel, (JsonBodyBuilder) (object) -> () -> {
            params.forEach(object::addProperty);
            return object;
        });
    }

    @Override
    public ResponseModel getResponse(RequestModel<HttpRequest> request) {
        try {
            HttpClient client = constructClient().client();
            HttpResponse<String> response = client.send(request.getRequest(),
                    HttpResponse.BodyHandlers.ofString());
            return new ResponseModel(
                    response.body(),
                    "",
                    response.statusCode()
            );
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseModel.EMPTY;
        }
    }

}
