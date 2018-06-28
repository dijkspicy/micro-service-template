package dijkspicy.ms.server.proxy.http;

import java.nio.file.Path;

/**
 * KeyStoreConfig
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class KeyStoreConfig {
    private Path path;
    private String storeType = "PKCS12";
    private String storePass = "Changeme_123";
    private String keyPass = "Changeme_123";

    public Path getPath() {
        return path;
    }

    public KeyStoreConfig setPath(Path path) {
        this.path = path;
        return this;
    }

    public String getStoreType() {
        return storeType;
    }

    public KeyStoreConfig setStoreType(String storeType) {
        this.storeType = storeType;
        return this;
    }

    public String getStorePass() {
        return storePass;
    }

    public KeyStoreConfig setStorePass(String storePass) {
        this.storePass = storePass;
        return this;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public KeyStoreConfig setKeyPass(String keyPass) {
        this.keyPass = keyPass;
        return this;
    }
}
