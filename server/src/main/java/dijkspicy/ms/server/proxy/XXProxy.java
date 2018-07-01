package dijkspicy.ms.server.proxy;

import com.google.inject.ImplementedBy;
import com.google.inject.Singleton;
import dijkspicy.ms.server.proxy.impl.XXProxyImpl;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/24
 */
@ImplementedBy(XXProxyImpl.class)
public interface XXProxy {
    int count(String type);
}
