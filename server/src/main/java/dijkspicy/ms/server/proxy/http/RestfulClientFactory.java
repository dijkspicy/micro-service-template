package dijkspicy.ms.server.proxy.http;

import java.nio.file.Path;
import java.util.function.Function;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * RestfulClientFactory
 *
 * @author dijkspicy
 * @date 2018/6/29
 */
public interface RestfulClientFactory {
    static RestfulClient createDefault() {
        return new RestfulClient();
    }

    static RestfulClient createWithEnvironment(EnvironmentConfig environment) {
        Environment.addEnv(environment);
        return new RestfulClient();
    }

    static RestfulClient createCloudSOPClient() {
        return new RestfulClient() {
            @Override
            protected CloseableHttpClient getHttpsClient(String host) {
                Function<Path, KeyStoreConfig> keyStoreMapping = path -> new KeyStoreConfig()
                        .setStoreType("PKCS12")
                        .setKeyPass("Changeme_123")
                        .setStorePass("Changeme_123")
                        .setPath(path.resolve("ssl/internal/server.p12"));
                Function<Path, TrustStoreConfig> trustStoreMapping = path -> new TrustStoreConfig()
                        .setStoreType("JKS")
                        .setStorePass("Changeme_123")
                        .setPath(path.resolve("ssl/internal/trust.jks"));
                return HttpClientFactory.createSSLFromHost(host, keyStoreMapping, trustStoreMapping);
            }
        };
    }
}
