package dijkspicy.ms.base;

import com.fasterxml.jackson.databind.type.TypeFactory;
import dijkspicy.ms.base.errors.InternalServerException;
import dijkspicy.ms.base.errors.SystemServerException;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.*;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Optional;


/**
 * BaseHandler
 *
 * @author dijkspicy
 * @date 2018/6/1
 */
public abstract class BaseHandler<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);

    /**
     * 执行逻辑
     *
     * @param request  请求
     * @param response 响应
     * @return 具体的返回
     */
    public final T execute(HttpServletRequest request, HttpServletResponse response) {
        XXXException exp;
        try (final Timer ignored = Timer.start(this)) {
            this.doPre(request);
            T out = this.doMainLogic(request, response);
            this.writeSuccessMessage();
            response.setStatus(HttpURLConnection.HTTP_OK);
            return out;
        } catch (XXXException e) {
            exp = e;
        } catch (Exception e) {
            exp = new InternalServerException("Unknown exception: " + e.getMessage(), e);
        } catch (Throwable e) {
            exp = new SystemServerException("Unknown error: " + e.getMessage(), e);
        } finally {
            this.doPost();
        }

        response.setStatus(exp.getHttpCode());
        this.writeFailureMessage(exp);
        LOGGER.error("-------------------ERROR-----------------\r\n" + this, exp);
        LOGGER.error("-----------------------------------------");
        return this.getResponseWithException(exp);
    }

    /**
     * 前置操作，一般用于参数校验和检查等
     *
     * @param request 有时候需要从请求中获取报文
     * @throws XXXException 参数校验失败
     */
    protected void doPre(HttpServletRequest request) throws XXXException {

    }

    /**
     * 主逻辑，用于正常的返回值逻辑
     *
     * @param request 请求体
     * @return 正常响应
     * @throws XXXException 逻辑异常
     */
    protected abstract T doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException;

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
     * @param exp 失败异常
     * @return 响应
     */
    protected T doFailureLogic(XXXException exp) {
        return null;
    }

    private T getResponseWithException(XXXException exp) {
        return Optional.ofNullable(this.doFailureLogic(exp)).orElseGet(() -> {
            Type type = this.getGenericType();
            Class<?> javaType = TypeFactory.rawClass(type);
            return this.getReflectionObject(exp, javaType);
        });
    }

    @SuppressWarnings("unchecked")
    private T getReflectionObject(XXXException exp, Class<?> rawClass) {
        if (!Returnable.class.isAssignableFrom(rawClass)) {
            return null;
        }

        ProxyFactory factory = new ProxyFactory();
        int modifiers = rawClass.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            factory.setInterfaces(new Class[]{rawClass});
        } else if (!Modifier.isFinal(modifiers)) {
            factory.setSuperclass(rawClass);
        } else {
            return (T) Returnable.RET_MAPPER.convertValue(exp, rawClass);
        }
        try {
            factory.setFilter(method -> Arrays.stream(Returnable.class.getMethods())
                    .anyMatch(it -> it.getName().equals(method.getName()))
            );
            return (T) factory.create(null, null, (o, method, method1, objects) -> {
                Optional<Method> optional = Arrays.stream(Returnable.class.getMethods())
                        .filter(it -> it.getName().equals(method.getName()))
                        .findAny();
                return optional.isPresent() ? optional.get().invoke(exp) : null;
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize returnable due to " + e.getMessage(), e);
        }
    }

    private Type getGenericType() {
        Type type = this.getClass().getGenericSuperclass();
        do {
            if (type instanceof Class) {
                if (type == BaseHandler.class) {
                    type = Returnable.class;
                    break;
                }
                type = ((Class) type).getGenericSuperclass();
                continue;
            }

            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType == BaseHandler.class) {
                    Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (!(actualType instanceof TypeVariable)) {
                        type = actualType;
                        break;
                    }
                } else {
                    type = rawType;
                    continue;
                }
            }

            throw new IllegalArgumentException("Concrete handler must has super handler with concrete T: " + type);
        } while (type != Object.class);
        return type;
    }

}

