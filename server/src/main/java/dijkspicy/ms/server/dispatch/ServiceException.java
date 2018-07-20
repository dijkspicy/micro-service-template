package dijkspicy.ms.server.dispatch;

import dijkspicy.ms.base.XXXException;

/**
 * ServiceException
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public class ServiceException extends XXXException {

    private static final long serialVersionUID = 2316826068144606907L;

    public ServiceException(String msg) {
        this(null, msg);
    }

    public ServiceException(String msg, Throwable e) {
        this(null, msg, e);
    }

    public ServiceException(Object data, String msg) {
        super(INTERNAL_SERVER_ERROR, data, msg);
    }

    public ServiceException(Object data, String msg, Throwable e) {
        super(INTERNAL_SERVER_ERROR, data, msg, e);
    }
}
