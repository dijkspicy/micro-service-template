package com.huawei.cloudsop.xxx.dispatch;

import com.huawei.cloudsop.common.HttpContext;
import com.huawei.cloudsop.xxx.common.Timer;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.common.errors.InternalServerException;
import com.huawei.cloudsop.xxx.model.XXXResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BaseHandler
 *
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler<T extends XXXResponse> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);
    private static final Map<Class<? extends BaseHandler>, Class<? extends XXXResponse>> GENERIC_TYPE = new ConcurrentHashMap<>();
    private final Class<T> responseType;

    @SuppressWarnings("unchecked")
    protected BaseHandler() {
        this.responseType = (Class<T>) GENERIC_TYPE.computeIfAbsent(this.getClass(), clazz -> {
            Type type = clazz;
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
                try {
                    return (Class<? extends XXXResponse>) ((Class<?>) t).newInstance().getClass();
                } catch (Exception e) {
                    throw new InternalServerException("Invalid generic response for handler: " + parameterizedType);
                }
            }
            return XXXResponse.class;
        });
    }

    /**
     * 执行逻辑
     *
     * @param context 请求/响应
     * @return 具体的返回
     */
    public final T execute(HttpContext context) {
        XXXException exp;
        try (final Timer ignored = Timer.start(this)) {
            this.doPre(context);
            T out = this.doMainLogic(context);
            this.writeSuccessMessage();
            context.setResponseStatus(HttpURLConnection.HTTP_OK);
            return out;
        } catch (XXXException e) {
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
        T out = this.getResponseWithException(exp);

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
     * @throws XXXException 参数校验失败
     */
    protected void doPre(HttpContext context) throws XXXException {

    }

    /**
     * 主逻辑，用于正常的返回值逻辑
     *
     * @param context 请求体
     * @return 正常响应
     * @throws XXXException 逻辑异常
     */
    protected abstract T doMainLogic(HttpContext context) throws XXXException;

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
    protected void writeFailureMessage(XXXException exp) {

    }

    private T getResponseWithException(XXXException exp) {
        try {
            T out = this.responseType.newInstance();
            out.setRetCode(exp.getRetCode());
            out.setRetInfo(exp.getRetInfo());
            return out;
        } catch (Exception e) {
            throw new InternalServerException("Failed to new instance of " + this.responseType.getSimpleName());
        }
    }
}

