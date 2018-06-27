package dijkspicy.ms.server.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dijkspicy.ms.server.proxy.impl.XXProxyImpl;

/**
 * Proxy
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public abstract class Proxy {

    private static final Injector INJECTOR = Guice.createInjector(binder -> {
        binder.bind(XXProxy.class).to(XXProxyImpl.class).asEagerSingleton();
    });

    public static <T> T getInstance(Class<T> proxyClass) {
        return INJECTOR.getInstance(proxyClass);
    }
}
