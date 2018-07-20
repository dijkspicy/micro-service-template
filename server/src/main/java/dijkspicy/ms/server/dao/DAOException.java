package dijkspicy.ms.server.dao;

import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class DAOException extends ServiceException {
    private static final long serialVersionUID = -8054721846382285781L;

    public DAOException(String msg) {
        super(null, msg);
    }

    public DAOException(String msg, Throwable e) {
        super(null, msg, e);
    }
}
