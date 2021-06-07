package evgenykuzin.core.util.http.models;

import com.github.evgenykuzin.core.util.http.headers.HeadersModel;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;

import java.util.Objects;

public abstract class RequestModel<T> {
    private final String url, requestString, jsonBody;
    private final HeadersModel headers;
    public static RequestModel EMPTY = new RequestModel("", "", "") {
        @Override
        public Object getRequest() {
            return null;
        }
    };

    public RequestModel(String url, String requestString, String jsonBody, HeadersModel headers) {
        this.url = url;
        this.requestString = requestString;
        this.jsonBody = jsonBody;
        this.headers = headers;
    }

    public RequestModel(String url, String requestString, String jsonBody) {
        this.url = url;
        this.requestString = requestString;
        this.jsonBody = jsonBody;
        this.headers = new HeadersModelImpl();
    }

    public String getUrl() {
        return url;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public abstract T getRequest();

    @Override
    public String toString() {
        return "RequestModel{" +
                "url='" + url + '\'' +
                ", requestString='" + requestString + '\'' +
                ", jsonBody='" + jsonBody + '\'' +
                ", headers=" + headers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestModel<?> that = (RequestModel<?>) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(requestString, that.requestString) &&
                Objects.equals(jsonBody, that.jsonBody) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, requestString, jsonBody, headers);
    }
}
