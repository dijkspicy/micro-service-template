package dijkspicy.ms.server.proxy;

import com.google.inject.Inject;
import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * ProxyException
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public class ProxyException extends ServiceException {
    public ProxyException(String msg) {
        super(PROXY_ERROR, msg);
    }

    public ProxyException(String msg, Throwable e) {
        super(PROXY_ERROR, msg, e);
    }
}
