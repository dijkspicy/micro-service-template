package dijkspicy.queeng.server.common;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonSerializer
 *
 * @author dijkspicy
 * @date 2018/6/17
 */
public interface JsonSerializer {
    Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);
    ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * 将指定java类从消息中反序列化出来
     *
     * @param message 消息
     * @param clazz   指定java类
     * @param <R>     指定java类
     * @return 指定java类对象
     * @throws IOException 反序列化失败
     */
    static <R> R deserialize(String message, Class<R> clazz) throws IOException {
        Optional.ofNullable(message).orElseThrow(() -> new IOException("Null value for " + clazz.getSimpleName()));
        return MAPPER.readValue(message, clazz);
    }

    /**
     * 将指定java对象序列化成字符串
     *
     * @param javaObj 指定java对象
     * @return 序列化后的字符串
     * @throws IOException 序列化失败
     */
    static String serialize(Object javaObj) throws IOException {
        Optional.ofNullable(javaObj).orElseThrow(() -> new IOException("Null value"));
        return MAPPER.writeValueAsString(javaObj);
    }

    /**
     * 将当前对象序列化为字符串
     *
     * @return 序列化后的字符串
     * @throws IOException 序列化失败
     */
    default String serialize() throws IOException {
        return serialize(this);
    }

    /**
     * 将当前对象用被序列化为字符串的对象覆盖
     *
     * @param message 某对象被序列化之后的字符串
     * @return 当前对象
     * @throws IOException 反序列化失败
     */
    default JsonSerializer deserialize(String message) throws IOException {
        final BiFunction<Class<?>, List<Field>, List<Field>> fieldsGetter = (clazz, fields) -> {
            while (clazz != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
            return fields;
        };

        List<Field> fields = fieldsGetter.apply(this.getClass(), new LinkedList<>());
        Object out = deserialize(message, this.getClass());
        if (out.getClass() == this.getClass()) {
            fields.forEach(it -> {
                // 忽略static类型
                if ((it.getModifiers() & Modifier.STATIC) == 0) {
                    boolean access = it.isAccessible();
                    it.setAccessible(true);
                    try {
                        it.set(this, it.get(out));
                    } catch (IllegalAccessException e) {
                        LOGGER.warn("Failed to set/get field(" + it.getName() + ") value when deserialize, error: " + e.getMessage());
                    }
                    it.setAccessible(access);
                }
            });
        }
        return this;
    }
}
