package dijkspicy.ms.server.dispatch;

import dijkspicy.ms.server.common.Timer;
import dijkspicy.ms.server.common.errors.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.function.Function;

/**
 * BaseHandler<T>
 *
 * @param <T> 一定是使用{@link ServiceResponse}类进行返回，如果有特殊需求，请继承response
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);
    private static final Function<Type, Class> GENERIC_MAPPING = type -> {
        ParameterizedType parameterizedType = null;
        do {
            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                if (BaseHandler.class == rawType) {
                    parameterizedType = (ParameterizedType) type;
                    break;
                } else if (rawType instanceof Class) {
                    type = ((Class) rawType).getGenericSuperclass();
                    continue;
                }
            }
            if (type instanceof Class) {
                type = ((Class) type).getGenericSuperclass();
                continue;
            }
            break;
        } while (type != null);

        if (parameterizedType != null && parameterizedType.getActualTypeArguments().length != 0) {
            Type t = parameterizedType.getActualTypeArguments()[0];
            if (t instanceof Class) {
                return (Class) t;
            }
        }
        return ServiceResponse.class;
    };

    private final Class<T> responseType;

    /**
     * 会自动获取泛型中的类，如果没有，则使用{@link ServiceResponse}
     */
    @SuppressWarnings("unchecked")
    protected BaseHandler() {
        this.responseType = GENERIC_MAPPING.apply(this.getClass());
    }

    /**
     * 执行逻辑
     *
     * @param context 请求/响应
     * @return 具体的返回
     */
    public final T execute(HttpContext context) {
        ServiceException exp;
        try (final AutoCloseable ignored = Timer.start(this)) {
            this.doPre(context);
            T out = this.doMainLogic(context);
            this.writeSuccessMessage();
            context.setResponseStatus(HttpURLConnection.HTTP_OK);
            return out;
        } catch (ServiceException e) {
            exp = e;
        } catch (Exception e) {
            exp = new InternalServerException("Unknown exception", e);
        } catch (Throwable e) {
            exp = new InternalServerException("Unknown error", e);
        } finally {
            this.doPost();
        }

        context.setResponseStatus(exp.getHttpCode());
        this.writeFailureMessage(exp);
        T out = this.getResponseWithException(context, exp);

        LOGGER.error("-------------------ERROR-----------------\r\n" + this, exp);
        LOGGER.error("-----------------------------------------");
        return out;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "<" + this.responseType.getSimpleName() + ">@" + this.hashCode() + "]";
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
                .orElseGet(() -> {
                    try {
                        return this.responseType.newInstance();
                    } catch (Exception e) {
                        LOGGER.info("Failed to new instance of " + this.responseType.getSimpleName());
                    }
                    return null;
                });
        if (out instanceof ServiceResponse) {
            ((ServiceResponse) out)
                    .setRetCode(exp.getRetCode())
                    .setRetInfo(exp.getRetInfo());
        }
        return out;
    }
}

