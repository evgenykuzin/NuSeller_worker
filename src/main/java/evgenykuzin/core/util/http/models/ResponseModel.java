package evgenykuzin.core.util.http.models;

import java.util.Objects;

public class ResponseModel {
    private final String responseString, status;
    private final int code;
    public static ResponseModel EMPTY = new ResponseModel("", "",0);

    public ResponseModel(String responeString, String status, int code) {
        this.responseString = responeString;
        this.status = status;
        this.code = code;
    }

    public String getResponseString() {
        return responseString;
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "responseString='" + responseString + '\'' +
                ", status='" + status + '\'' +
                ", code=" + code +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseModel that = (ResponseModel) o;
        return code == that.code &&
                Objects.equals(responseString, that.responseString) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseString, status, code);
    }
}