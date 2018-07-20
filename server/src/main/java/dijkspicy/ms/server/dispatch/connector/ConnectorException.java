package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.base.Returnable;
import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class ConnectorException extends ServiceException {
    private static final long serialVersionUID = -2823223211149422528L;
    private static final Ret CONNECTION_ERROR = new Returnable.Ret(500, 2001, "Connection Error");

    public ConnectorException(String msg) {
        super(CONNECTION_ERROR, msg);
    }

    public ConnectorException(String msg, Throwable e) {
        super(CONNECTION_ERROR, msg, e);
    }
}
