package dijkspicy.ms.server.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Proxy
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public final class Proxy {
    private static final Injector INJECTOR = Guice.createInjector(binder -> {

    });

    public static <T> T getInstance(Class<T> proxyClass) {
        try {
            return INJECTOR.getInstance(proxyClass);
        } catch (Exception e) {
            throw new ProxyException("You forgot register proxy: " + proxyClass.getSimpleName(), e);
        }
    }
}
