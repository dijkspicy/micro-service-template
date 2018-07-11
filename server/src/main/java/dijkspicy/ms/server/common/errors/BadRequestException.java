package dijkspicy.ms.server.common.errors;

import dijkspicy.ms.server.dispatch.ServiceException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class BadRequestException extends ServiceException {
    private static final long serialVersionUID = 1714337122516797732L;

    public BadRequestException(String msg) {
        super(BAD_REQUEST, msg);
    }

    public BadRequestException(String msg, Throwable e) {
        super(BAD_REQUEST, msg, e);
    }
}
