package dijkspicy.queeng.server.common;

import java.text.MessageFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseResources
 *
 * @author dijkspicy
 * @date 2018/5/25
 */
public abstract class BaseResources {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseResources.class);
    private final String bundleName;
    private ResourceBundle bundle;

    protected BaseResources(String bundleName) {
        this.bundleName = bundleName;
        this.bundle = this.createBundle();
    }

    public static BaseResources create(String bundleName) {
        return new BaseResources(bundleName) {
        };
    }

    public String getString(String key) {
        try {
            Objects.requireNonNull(key, "Message key can not be null");
            return this.getStringFromBundle(key);
        } catch (MissingResourceException e) {
            return this.getDefault(key);
        }
    }

    public String getString(String key, Object... args) {
        String pattern = this.getString(key);
        try {
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            LOGGER.warn("Failed to format key: {} with {}", key, Arrays.toString(args));
            return pattern;
        }
    }

    private String getStringFromBundle(String key) {
        if (this.bundle == null) {
            this.bundle = this.createBundle();
            if (this.bundle == null) {
                return this.getDefault(key);
            }
        }
        return this.bundle.getString(key);
    }

    private String getDefault(String key) {
        return '!' + key + '!';
    }

    private ResourceBundle createBundle() {
        try {
            return ResourceBundle.getBundle(this.bundleName, Locale.getDefault(), BaseResources.class.getClassLoader());
        } catch (Throwable t) {
            try {
                return ResourceBundle.getBundle(this.bundleName);
            } catch (Throwable t2) {
                LOGGER.error("Can't load resource bundle due to underlying exception " + t.toString(), t2);
            }
        }
        return null;
    }
}
