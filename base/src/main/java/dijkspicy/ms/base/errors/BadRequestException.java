package dijkspicy.ms.base.errors;


import dijkspicy.ms.base.XXXException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class BadRequestException extends XXXException {
    private static final long serialVersionUID = 1714337122516797732L;

    public BadRequestException(Object data, String msg) {
        super(BAD_REQUEST, data, msg);
    }

    public BadRequestException(Object data, String msg, Throwable e) {
        super(BAD_REQUEST, data, msg, e);
    }

    public BadRequestException(String msg) {
        this(null, msg);
    }

    public BadRequestException(String msg, Throwable e) {
        this(null, msg, e);
    }
}
