package evgenykuzin.core.util.http.services;

import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.HeadersModel;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.models.ClientModel;
import com.github.evgenykuzin.core.util.http.models.RequestModel;
import com.github.evgenykuzin.core.util.http.models.ResponseModel;

import java.util.Map;

public interface HttpService<R, C> {
    String POST = "POST";
    String GET = "GET";
    String PUT = "PUT";
    String DELETE = "DELETE";
    String TYPE_JSON = "application/json; charset=UTF-8";
    ClientModel<C> constructClient();
    RequestModel<R> constructRequest(String url, String method, HeadersModel headers, BodyBuilder bodyBuilder);
    ResponseModel getResponse(RequestModel<R> request);

    default RequestModel<R> constructGetRequest(String url, String... params) {
        if (params.length > 0) url += "?";
        StringBuilder urlBuilder = new StringBuilder(url);
        for (int i = 0; i < params.length; i++) {
            urlBuilder.append(params[i]);
            if (i != params.length - 1) urlBuilder.append("&");
        }
        return constructRequest(urlBuilder.toString(), GET, new HeadersModelImpl(), BodyBuilder.NO_BODY);
    }

    default RequestModel<R> constructGetRequest(String url, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            url += "?";
            StringBuilder urlBuilder = new StringBuilder(url);
            params.forEach((k, v) -> urlBuilder
                    .append(k)
                    .append("=")
                    .append(v)
                    .append("&"));
            urlBuilder.deleteCharAt(urlBuilder.lastIndexOf("&"));
            url = urlBuilder.toString();
        }
        return constructRequest(url, GET, new HeadersModelImpl(), BodyBuilder.NO_BODY);
    }
}
