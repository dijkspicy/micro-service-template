package dijkspicy.queeng.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * dijkspicy-commons
 *
 * @author dijkspicy
 * @date 2018/5/25
 */
public abstract class BaseResourcesHolder {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseResourcesHolder.class);
    private final ResourceBundle bundle;
    private final String bundleName;

    protected BaseResourcesHolder(String bundleName) {
        this.bundleName = bundleName;
        ResourceBundle temp = null;
        try {
            temp = ResourceBundle.getBundle(bundleName, Locale.getDefault(), BaseResourcesHolder.class.getClassLoader());
        } catch (Throwable t) {
            try {
                temp = ResourceBundle.getBundle(bundleName);
            } catch (Throwable t2) {
                throw new RuntimeException("Can't load resource bundle due to underlying exception " + t.toString(), t2);
            }
        } finally {
            this.bundle = temp;
        }

        if (this.bundle == null) {
            throw new RuntimeException("Localized messages from resource bundle '" + this.bundleName + "' not loaded during initialization.");
        }
    }

    public static BaseResourcesHolder create(String bundleName) {
        return new BaseResourcesHolder(bundleName) {
        };
    }

    public String getString(String key) {
        try {
            key = OptionalString.of(key)
                    .orElseThrow(() -> new IllegalArgumentException("Message key can not be null"));
            return this.bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public String getString(String key, Object... args) {
        String pattern = null;
        try {
            pattern = this.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            LOGGER.info("Failed to format key: " + key + " with " + Arrays.toString(args));
            return pattern;
        }
    }
}
