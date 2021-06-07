package evgenykuzin.core.util.http.headers;

import java.util.Objects;

public class Header {
    private String key;
    private String value;
    public static final Header EMPTY = new Header("", "");
    public static final Header APP_JSON = new Header("Content-Type", "application/json");
    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Header{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return Objects.equals(key, header.key) &&
                Objects.equals(value, header.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
