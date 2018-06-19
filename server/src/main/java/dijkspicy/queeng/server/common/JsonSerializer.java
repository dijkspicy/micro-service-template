package dijkspicy.queeng.server.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * queeng
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

    static <R> R deserialize(String s, Class<R> clazz) throws IOException {
        Optional.ofNullable(s).orElseThrow(() -> new IOException("Null value for " + clazz.getSimpleName()));
        return MAPPER.readValue(s, clazz);
    }

    static String serialize(Object javaObj) throws IOException {
        Optional.ofNullable(javaObj).orElseThrow(() -> new IOException("Null value"));
        return MAPPER.writeValueAsString(javaObj);
    }

    default String serialize() throws IOException {
        return serialize(this);
    }

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
