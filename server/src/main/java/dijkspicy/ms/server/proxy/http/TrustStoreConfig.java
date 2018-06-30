package dijkspicy.ms.server.proxy.http;

import java.nio.file.Path;

/**
 * TrustStoreConfig
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class TrustStoreConfig {
    private Path path;
    private String storeType = "JKS";
    private String storePass = "Changeme_123";

    public Path getPath() {
        return path;
    }

    public TrustStoreConfig setPath(Path path) {
        this.path = path;
        return this;
    }

    public String getStoreType() {
        return storeType;
    }

    public TrustStoreConfig setStoreType(String storeType) {
        this.storeType = storeType;
        return this;
    }

    public String getStorePass() {
        return storePass;
    }

    public TrustStoreConfig setStorePass(String storePass) {
        this.storePass = storePass;
        return this;
    }

    @Override
    public String toString() {
        return "TrustStoreConfig{" +
                "path=" + path +
                ", storeType='" + storeType + '\'' +
                ", storePass='" + storePass + '\'' +
                '}';
    }
}
