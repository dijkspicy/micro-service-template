package dijkspicy.ms.server.common.errors;

import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class InternalServerException extends ServiceException {
    private static final long serialVersionUID = -6306565730694110177L;

    public InternalServerException(String msg) {
        super(INTERNAL_SERVER_ERROR, msg);
    }

    public InternalServerException(String msg, Throwable e) {
        super(INTERNAL_SERVER_ERROR, msg, e);
    }
}
