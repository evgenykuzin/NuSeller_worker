package evgenykuzin.core.db.dao;

import com.github.evgenykuzin.core.db.hibernate.HibernateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.SessionFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public abstract class AbstractDAO<C, I extends Serializable> implements DAO<C, I> {
    private final Class<C> entityClass;
    private final Supplier<SessionFactory> sessionFactorySupplier;

    public AbstractDAO(Class<C> entityClass) {
        this.entityClass = entityClass;
        this.sessionFactorySupplier = HibernateUtil::getSessionFactory;
    }

    public void saveAll(@NotNull final Collection<C> objects) {
        executeVoid(session -> objects.forEach(session::save));
    }

    public void save(C object) {
        saveAll(Collections.singleton(object));
    }

    public C get(I id) {
        return executeAndGet(session -> session
                .get(entityClass, id));
    }

    public List<C> getAll() {
        return executeAndGet(session -> {
            var cq = session
                    .getCriteriaBuilder()
                    .createQuery(entityClass);
            var all = cq
                    .select(cq.from(entityClass));
            return session.createQuery(all).getResultList();
        });
    }

    public List<C> searchBy(SearchEntry... searchEntries) {
        return searchBy(getTableName(), searchEntries);
    }

    protected List<C> searchBy(String tableName, SearchEntry... searchEntries) {
        return executeAndGet(session -> {
            String queryString = new SelectQueryBuilder()
                    .selectAllFrom(tableName)
                    .where(searchEntries)
                    .build();
            var query = session
                    .createNativeQuery(queryString, entityClass);
            return query.getResultList();
        });
    }

    protected C getBy(SearchEntry... searchEntries) {
        var search = searchBy(searchEntries);
        if (search == null || search.isEmpty()) {
            return null;
        }
        return search.get(0);
    }

    public void updateAll(Collection<C> objects) {
        executeVoid(session -> objects.forEach(session::update));
    }

    public void update(C object) {
        updateAll(Collections.singleton(object));
    }

    public void deleteAll(Collection<C> objects) {
        executeVoid(session -> objects.forEach(session::delete));
    }

    public void delete(C object) {
        deleteAll(Collections.singleton(object));
    }

    @Override
    public void saveOrUpdate(C object) {
        executeVoid(session -> session.saveOrUpdate(object));
    }

    protected void executeVoid(Command command) {
        this.executeVoid(sessionFactorySupplier.get(), command);
    }

    protected <T> T executeAndGet(Getter<T> getter) {
        return this.executeAndGet(sessionFactorySupplier.get(), getter);
    }

    public abstract String getTableName();

    public static class SelectQueryBuilder {
        private final StringBuilder queryStringBuilder;

        public SelectQueryBuilder() {
            queryStringBuilder = new StringBuilder();
        }

        public SelectQueryBuilder selectAllFrom(String tableName) {
            queryStringBuilder
                    .append("select * from ")
                    .append(tableName);
            return this;
        }

        public SelectQueryBuilder where(SearchEntry... searchEntries) {
            queryStringBuilder.append(" where ");
            int entriesCount = searchEntries.length;
            int counter = 0;
            for (SearchEntry searchEntry : searchEntries) {
                counter++;
                queryStringBuilder
                        .append(searchEntry.getKeyName())
                        .append(" = '")
                        .append(searchEntry.getKeyValue())
                        .append("' ");
                if (counter < entriesCount) queryStringBuilder.append("and ");
            }
            return this;
        }

        public String build() {
            return queryStringBuilder.toString();
        }
    }

}
