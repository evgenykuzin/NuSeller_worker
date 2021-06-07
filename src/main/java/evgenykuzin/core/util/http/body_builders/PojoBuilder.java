package evgenykuzin.core.util.http.body_builders;

import com.github.evgenykuzin.core.util.http.pojos.Pojo;

public class PojoBuilder implements BodyBuilder {
    Pojo pojo;

    public PojoBuilder(Pojo pojo) {
        this.pojo = pojo;
    }

    @Override
    public String getJsonString() {
        return pojo.getJsonString();
    }

}
