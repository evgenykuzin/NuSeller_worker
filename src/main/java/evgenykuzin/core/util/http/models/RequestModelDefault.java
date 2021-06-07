package evgenykuzin.core.util.http.models;

import com.github.evgenykuzin.core.util.http.headers.HeadersModel;

public class RequestModelDefault<T> extends RequestModel<T> {
    private final T request;
    public RequestModelDefault(String url, String requestString, String jsonBody, HeadersModel headers, T request) {
        super(url, requestString, jsonBody, headers);
        this.request = request;
    }

    @Override
    public T getRequest() {
        return request;
    }
}
