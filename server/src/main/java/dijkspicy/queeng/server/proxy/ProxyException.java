package dijkspicy.queeng.server.proxy;

import dijkspicy.queeng.server.dispatch.ServiceException;

/**
 * ODAEQueryEngineService
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public class ProxyException extends ServiceException {
    private static final long serialVersionUID = 8976332509969703397L;

    public ProxyException(String msg) {
        super(PROXY_ERROR, msg);
    }

    public ProxyException(String msg, Throwable e) {
        super(PROXY_ERROR, msg, e);
    }
}