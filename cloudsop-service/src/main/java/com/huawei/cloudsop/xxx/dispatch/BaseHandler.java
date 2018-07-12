package com.huawei.cloudsop.xxx.dispatch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.huawei.bsp.common.HttpContext;
import com.huawei.cloudsop.xxx.common.Timer;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.common.XXXResponse;
import com.huawei.cloudsop.xxx.common.errors.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Optional;

/**
 * BaseHandler
 *
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Injector INJECTOR = Guice.createInjector(binder -> {
        binder.bind(Boolean.class).toInstance(false);
        binder.bind(Byte.class).toInstance((byte) 0);
        binder.bind(Character.class).toInstance('0');
        binder.bind(Double.class).toInstance(0D);
        binder.bind(Float.class).toInstance(0F);
        binder.bind(Integer.class).toInstance(0);
        binder.bind(Long.class).toInstance(0L);
        binder.bind(Number.class).toInstance(0);
        binder.bind(Short.class).toInstance((short) 0);
    });

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
            exp = new InternalServerException("Unknown exception: " + e.getMessage(), e);
        } catch (Throwable e) {
            exp = new InternalServerException("Unknown error: " + e.getMessage(), e);
        } finally {
            this.doPost();
        }

        context.setResponseStatus(exp.getHttpCode());
        this.writeFailureMessage(exp);
        LOGGER.error("-------------------ERROR-----------------\r\n" + this, exp);
        LOGGER.error("-----------------------------------------");
        return this.getResponseWithException(context, exp);
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

    /**
     * 失败之后如果需要有特殊的返回，则重载该方法进行自定义
     *
     * @param context 请求体
     * @param exp     失败异常
     * @return 响应
     */
    protected T doFailureLogic(HttpContext context, XXXException exp) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private T getResponseWithException(HttpContext context, XXXException exp) {
        T out = Optional.ofNullable(this.doFailureLogic(context, exp))
                .orElseGet(() -> {
                    Type type = this.getClass().getGenericSuperclass();
                    do {
                        if (type instanceof Class) {
                            if (type == BaseHandler.class) {
                                type = ParameterizedTypeImpl.make(BaseHandler.class, new Type[]{XXXResponse.class}, null);
                                break;
                            }
                            type = ((Class) type).getGenericSuperclass();
                            continue;
                        } else if (type instanceof ParameterizedType) {
                            Type rawType = ((ParameterizedType) type).getRawType();
                            if (rawType instanceof Class) {
                                if (rawType == BaseHandler.class) {
                                    break;
                                }
                                type = ((Class) rawType).getGenericSuperclass();
                                continue;
                            }
                        }
                        throw new IllegalArgumentException("Concrete handler must has super handler with concrete T: " + type);
                    } while (type != Object.class);

                    JavaType javaType = TypeFactory.defaultInstance()
                            .constructType(((ParameterizedType) type).getActualTypeArguments()[0]);
                    try {
                        return javaType instanceof CollectionLikeType || javaType instanceof ArrayType
                                ? OBJECT_MAPPER.readValue("[]", javaType)
                                : javaType instanceof MapLikeType
                                ? OBJECT_MAPPER.readValue("{}", javaType)
                                : (T) INJECTOR.getInstance(javaType.getRawClass());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid generic response for handler: " + type, e);
                    }
                });
        if (out instanceof XXXResponse) {
            ((XXXResponse) out).setRetCode(exp.getRetCode());
            ((XXXResponse) out).setRetInfo(exp.getRetInfo());
        }
        return out;
    }
}

