package dijkspicy.ms.server.dispatch.dao;

import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.HttpContext;
import dijkspicy.ms.server.dispatch.ServiceException;
import dijkspicy.ms.server.dispatch.ServiceResponse;
import dijkspicy.ms.server.persistence.DAO;
import dijkspicy.ms.server.persistence.XXDAO;

/**
 * DAOHandler
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class DAOHandler extends BaseHandler<ServiceResponse> {
    public DAOHandler(String type) {
    }

    @Override
    protected ServiceResponse doMainLogic(HttpContext context) throws ServiceException {
        XXDAO out = DAO.getInstance(XXDAO.class);
        LOGGER.error(out.toString());
        return new ServiceResponse();
    }
}
