package dijkspicy.ms.server.proxy.linux;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Project: MSO
 * <p>
 * Created by t00321127 on 2017/3/24.
 */
public enum StringSerializer {
    forYaml(new ObjectMapper()),
    forShow(new ObjectMapper());

    // ObjectMapper是线程安全的，应该尽量的重用
    private final ObjectMapper mapper;

    StringSerializer(ObjectMapper mapper) {
        this.mapper = mapper
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public <T> T deserialize(String jsonStr, Class<T> type) throws RuntimeException {
        try {
            return mapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new RuntimeException("failed to deserialize " + jsonStr + "\nmessage: " + e.getMessage(), e);
        }
    }

    public <T> T deserialize(String jsonStr, TypeReference<T> type) throws RuntimeException {
        try {
            return mapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new RuntimeException("failed to deserialize " + jsonStr + "\nmessage: " + e.getMessage(), e);
        }
    }

    public String serialize(Object obj) throws RuntimeException {
        if (obj == null || obj instanceof String) {
            return String.valueOf(obj);
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("failed to serialize, " + e.getMessage(), e);
        }
    }
}
