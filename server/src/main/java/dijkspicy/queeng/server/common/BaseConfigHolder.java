package dijkspicy.queeng.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Function;

/**
 * dijkspicy-commons
 *
 * @author dijkspicy
 * @date 2018/5/25
 */
public abstract class BaseConfigHolder {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseConfigHolder.class);
    private final Properties properties = new Properties();
    private final String file;

    protected BaseConfigHolder(Path path) {
        this.file = path.getFileName().toString();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try (InputStream is = path.toUri().toURL().openStream()) {
                this.properties.load(is);
            } catch (IOException e) {
                LOGGER.warn("Failed to load properties file " + this.file, e);
            }
        } else {
            LOGGER.warn("Failed to find config file: " + this.file);
        }
    }

    public static BaseConfigHolder create(Path path) {
        return new BaseConfigHolder(path) {
        };
    }

    public int getInt(String key, int defaultValue) {
        return this.getOrDefault(key, defaultValue, v -> {
            try {
                return Integer.valueOf(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        });
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.getOrDefault(key, defaultValue, Boolean::valueOf);
    }

    public long getLong(String key, long defaultValue) {
        return this.getOrDefault(key, defaultValue, v -> {
            try {
                return Long.valueOf(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        });
    }

    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    private <T> T getOrDefault(String key, T defaultValue, Function<String, T> valueParser) {
        String value = this.properties.getProperty(key);
        if (OptionalString.of(value).isPresent()) {
            return valueParser.apply(value);
        }
        LOGGER.warn("There is no value of key: {} in {}", key, this.file);
        return defaultValue;
    }
}
