package evgenykuzin.core.util.http.headers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HeadersModelImpl implements HeadersModel {
    Set<Header> headersSet;

    public HeadersModelImpl(Set<Header> headersSet) {
        this.headersSet = headersSet;
    }

    public HeadersModelImpl() {
        this(new HashSet<>());
    }

    public HeadersModelImpl(String headersText) {
        this();
        var headersLines = headersText.split("\n");
        for (String headerLine : headersLines) {
            var splitHeader = headerLine.split(":");
            if (splitHeader.length < 2) continue;
            var key = splitHeader[0].trim();
            var value = splitHeader[1].trim();
            headersSet.add(new Header(key, value));
        }
    }

    public HeadersModelImpl(Header header) {
        this();
        headersSet.add(header);
    }

    public HeadersModelImpl(String key, String value) {
        this(new Header(key, value));
    }

    public HeadersModelImpl(Header... headers) {
        this();
        headersSet.addAll(Arrays.asList(headers));
    }

    @Override
    public Set<Header> getSet() {
        return headersSet;
    }

    @Override
    public Header get(String key) {
        for (Header header : headersSet) {
            if (header.getKey().equals(key)) return header;
        }
        return Header.EMPTY;
    }

    @Override
    public void add(Header header) {
        headersSet.add(header);
    }

    @Override
    public void add(String key, String value) {
        add(new Header(key, value));
    }

    @Override
    public String toString() {
        return "HeadersModelImpl{" +
                "headersSet=" + headersSet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeadersModelImpl that = (HeadersModelImpl) o;
        return Objects.equals(headersSet, that.headersSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headersSet);
    }
}
