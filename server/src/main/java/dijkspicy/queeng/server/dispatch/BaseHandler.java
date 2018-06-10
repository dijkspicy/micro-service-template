package dijkspicy.queeng.server.dispatch;

import dijkspicy.queeng.server.common.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * queeng
 *
 * @param <T> 一定是使用{@link Response}类进行返回，如果有特殊需求，请继承response
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);
    private final Class<T> responseType;

    /**
     * 会自动获取泛型中的类，如果没有，则使用{@link Response}
     */
    @SuppressWarnings("unchecked")
    protected BaseHandler() {
        ParameterizedType type = this.getType(this.getClass());
        this.responseType = (Class<T>) (type != null && type.getActualTypeArguments().length != 0
                ? type.getActualTypeArguments()[0]
                : Response.class);
    }

    /**
     * 执行逻辑
     *
     * @param context 请求/响应
     * @return 具体的返回
     */
    public final T execute(HttpContext context) {
        T out = null;
        ServiceException exp = null;
        try (Timer ignored = new Timer(this)) {
            this.doPre(context);
            out = this.doMainLogic(context);
            this.writeSuccessMessage();
            context.setResponseStatus(HttpURLConnection.HTTP_OK);
        } catch (ServiceException e) {
            exp = e;
        } catch (Exception e) {
            exp = new ServiceException("Unknown exception", e);
        } catch (Throwable e) {
            exp = new ServiceException("Unknown error", e);
        } finally {
            this.doPost();
        }

        if (exp != null) {
            context.setResponseStatus(exp.getHttpCode());
            this.writeFailureMessage(exp);
            out = this.getResponseWithException(context, exp);

            LOGGER.error("\r\n-------------------ERROR-----------------");
            LOGGER.error("\r\n" + this + "\r\n" + exp.getMessage(), exp);
            LOGGER.error("\r\n-----------------------------------------");
        }
        return out;
    }

    /**
     * 前置操作，一般用于参数校验和检查等
     *
     * @param context 有时候需要从请求中获取报文
     * @throws ServiceException 参数校验失败
     */
    protected void doPre(HttpContext context) throws ServiceException {

    }

    /**
     * 主逻辑，用于正常的返回值逻辑
     *
     * @param context 请求体
     * @return 正常响应
     * @throws ServiceException 逻辑异常
     */
    protected abstract T doMainLogic(HttpContext context) throws ServiceException;

    /**
     * 写成功操作日志
     */
    protected void writeSuccessMessage() {

    }

    /**
     * 后置操作，比如释放资源，删除临时文件等，无论是成功还是失败均需要执行该操作
     */
    protected void doPost() {

    }

    /**
     * 写失败操作日志
     *
     * @param exp 异常
     */
    protected void writeFailureMessage(ServiceException exp) {

    }

    /**
     * 失败之后如果需要有特殊的返回，则重载该方法进行自定义
     *
     * @param context 请求体
     * @param exp     失败异常
     * @return 响应
     */
    protected T doFailureLogic(HttpContext context, ServiceException exp) {
        return null;
    }

    private T getResponseWithException(HttpContext context, ServiceException exp) {
        T out = Optional.ofNullable(this.doFailureLogic(context, exp))
                .orElseGet(this::getDefaultResponse);
        if (out instanceof Response) {
            ((Response) out)
                    .setResult("ERROR")
                    .setMessage(exp.getId() + ": " + exp.getMessage());
        }
        return out;
    }

    private ParameterizedType getType(Type type) {
        return type instanceof ParameterizedType && BaseHandler.class == ((ParameterizedType) type).getRawType()
                ? (ParameterizedType) type
                : type instanceof Class
                ? this.getType(((Class) type).getGenericSuperclass())
                : null;
    }

    private T getDefaultResponse() {
        try {
            return this.responseType.newInstance();
        } catch (Exception e) {
            LOGGER.info("Failed to new instance of " + this.responseType.getSimpleName());
        }
        return null;
    }

    /**
     * response
     */
    public static class Response {
        private String result = "OK";
        private String message;

        public String getResult() {
            return result;
        }

        public Response setResult(String result) {
            this.result = result;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Response setMessage(String message) {
            this.message = message;
            return this;
        }
    }
}

