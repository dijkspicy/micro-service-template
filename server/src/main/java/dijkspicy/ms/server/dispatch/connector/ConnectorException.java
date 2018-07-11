package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class ConnectorException extends ServiceException {
    private static final long serialVersionUID = -2823223211149422528L;

    public ConnectorException(String msg) {
        super(CONNECTION_ERROR, msg);
    }

    public ConnectorException(String msg, Throwable e) {
        super(CONNECTION_ERROR, msg, e);
    }
}
