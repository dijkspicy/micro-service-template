package dijkspicy.ms.server.dispatch.dao;

import dijkspicy.ms.base.XXXException;
import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.ServiceResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DAOHandler
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class DAOHandler extends BaseHandler {
    public DAOHandler(String type) {
    }

    @Override
    protected ServiceResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
        return new ServiceResponse();
    }
}
