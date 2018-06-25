package dijkspicy.ms.server.dispatch.proxy;

import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.HttpContext;
import dijkspicy.ms.server.dispatch.ServiceException;
import dijkspicy.ms.server.dispatch.ServiceResponse;
import dijkspicy.ms.server.proxy.Proxy;
import dijkspicy.ms.server.proxy.XXProxy;

/**
 * ProxyHandler
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class ProxyHandler extends BaseHandler<ServiceResponse> {
    public ProxyHandler(String type) {
    }

    @Override
    protected ServiceResponse doMainLogic(HttpContext context) throws ServiceException {
        XXProxy out = Proxy.getInstance(XXProxy.class);
        LOGGER.error(out.toString());
        return new ServiceResponse();
    }
}
