package dijkspicy.ms.server.persistence;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dijkspicy.ms.server.persistence.impl.XXDAOImpl;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public abstract class DAO {

    private static final Injector INJECTOR = Guice.createInjector(binder -> {
        binder.bind(XXDAO.class).to(XXDAOImpl.class).asEagerSingleton();
    });

    public static <T> T getInstance(Class<T> proxyClass) {
        return INJECTOR.getInstance(proxyClass);
    }
}
