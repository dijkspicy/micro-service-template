package dijkspicy.ms.base;

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
public abstract class BaseConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseConfig.class);
    private final String file;
    private final Path path;
    private Properties properties;

    protected BaseConfig(Path path) {
        this.path = path;
        this.file = path.getFileName().toString();
        this.properties = this.createProperties();
    }

    public static BaseConfig create(Path path) {
        return new DefaultConfig(path);
    }

    public int getInt(String key, int defaultValue) {
        return this.getOrDefault(key, defaultValue, v -> {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        });
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.getOrDefault(key, defaultValue, Boolean::parseBoolean);
    }

    public long getLong(String key, long defaultValue) {
        return this.getOrDefault(key, defaultValue, v -> {
            try {
                return Long.parseLong(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        });
    }

    public String getProperty(String key, String defaultValue) {
        if (this.properties == null) {
            this.properties = this.createProperties();
            if (this.properties == null) {
                return defaultValue;
            }
        }
        return this.properties.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return this.getProperty(key, null);
    }

    private Properties createProperties() {
        if (Files.exists(this.path) && Files.isRegularFile(this.path)) {
            try (InputStream is = this.path.toUri().toURL().openStream()) {
                Properties temp = new Properties();
                temp.load(is);
                return temp;
            } catch (IOException e) {
                LOGGER.warn("Failed to load properties file " + this.file);
            }
        } else {
            LOGGER.warn("Failed to find config file: " + this.file);
        }
        return null;
    }

    private <T> T getOrDefault(String key, T defaultValue, Function<String, T> valueParser) {
        String value = this.getProperty(key);
        if (value != null) {
            return valueParser.apply(value);
        }
        LOGGER.warn("There is no value of key: {} in {}", key, this.file);
        return defaultValue;
    }

    private static class DefaultConfig extends BaseConfig {

        private DefaultConfig(Path path) {
            super(path);
        }
    }
}
