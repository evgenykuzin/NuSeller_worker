package evgenykuzin.core.db.hibernate;

import com.github.evgenykuzin.core.entities.Dimensions;
import com.github.evgenykuzin.core.entities.Price;
import com.github.evgenykuzin.core.entities.Stock;
import com.github.evgenykuzin.core.entities.product.OzonProduct;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.WildeberriesProduct;
import com.github.evgenykuzin.core.entities.product.YamarketProduct;
import com.github.evgenykuzin.core.util_managers.PropertiesManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                Properties settings = PropertiesManager.getProperties("database");
                settings.setProperty(Environment.HBM2DDL_AUTO, "update");
                settings.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

                configuration
                        .addPackage("com.github.evgenykuzin.core.entities");

                configuration
                        .setProperties(settings);

                configuration
                        .addAnnotatedClass(Product.class)
                        .addAnnotatedClass(OzonProduct.class)
                        .addAnnotatedClass(YamarketProduct.class)
                        .addAnnotatedClass(WildeberriesProduct.class)
                        .addAnnotatedClass(Dimensions.class)
                        .addAnnotatedClass(Stock.class)
                        .addAnnotatedClass(Price.class);


                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    private HibernateUtil() {
    }

    public static void close() {
        sessionFactory.close();
    }
}
