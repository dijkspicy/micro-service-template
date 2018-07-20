package dijkspicy.ms.server.dispatch;

/**
 * BaseHandler<T>
 *
 * @param <T> 一定是使用{@link ServiceResponse}类进行返回，如果有特殊需求，请继承response
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler extends dijkspicy.ms.base.BaseHandler<ServiceResponse> {

    public final ServiceResponse execute(HttpContext context) {
        return super.execute(context.getHttpServletRequest(), context.getHttpServletResponse());
    }
}

