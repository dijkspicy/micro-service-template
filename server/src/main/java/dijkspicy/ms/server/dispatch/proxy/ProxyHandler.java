package dijkspicy.ms.server.dispatch.proxy;

import dijkspicy.ms.base.XXXException;
import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.ServiceResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ProxyHandler
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class ProxyHandler extends BaseHandler {
    public ProxyHandler(String type) {
    }

    @Override
    protected ServiceResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
        return new ServiceResponse();
    }
}
