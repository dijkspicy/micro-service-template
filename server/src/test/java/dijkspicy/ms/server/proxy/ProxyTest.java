package dijkspicy.ms.server.proxy;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.junit.Test;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class ProxyTest {
    @Inject
    private XXProxy proxy;

    @Inject
    private XXProxy proxy2;

    @Test
    public void getInstance() throws NoSuchMethodException {
        ProxyTest proxyTest = Guice.createInjector().getInstance(ProxyTest.class);
        System.out.println(proxyTest.proxy.equals(proxyTest.proxy2));
        System.out.println(Guice.createInjector().getInstance(XXProxy.class).count("adsf"));
    }
}