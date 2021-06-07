package evgenykuzin.core.util.http.headers;

import java.util.Set;

public interface HeadersModel {
    HeadersModel EMPTY_IMPL = new HeadersModelImpl();
    Set<Header> getSet();
    Header get(String key);
    void add(Header header);
    void add(String key, String value);
    default boolean isEmpty() {return getSet().isEmpty();}
}
