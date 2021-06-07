package evgenykuzin.core.util.http.pojos;

public interface PojoDefault extends Pojo {
    @Override
    default String getJsonString() {
        return gsonParse();
    }
}
