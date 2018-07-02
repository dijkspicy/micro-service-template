package dijkspicy.ms.server.dao;

import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class DAOException extends ServiceException {
    public DAOException(String msg) {
        super(DAO_ERROR, msg);
    }

    public DAOException(String msg, Throwable e) {
        super(DAO_ERROR, msg, e);
    }
}
