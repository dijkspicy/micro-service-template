package dijkspicy.ms.server.proxy.http;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.sun.istack.internal.NotNull;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClientFactory
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public abstract class HttpClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);
    private static final Map<String, CloseableHttpClient> HTTP_CLIENT_MAP = new ConcurrentHashMap<>();
    private static final String[] SUPPORTED_PROTOCOLS = {
            "TLSv1",
            "TLSv1.1",
            "TLSv1.2"
    };
    private static final String[] SUPPORTED_CIPHER_SUITES = {
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA"
    };
    private static final HostnameVerifiersEnum VERIFIERS = HostnameVerifiersEnum.NONE;

    private HttpClientFactory() {
    }

    public static CloseableHttpClient createDefault() {
        return HttpClients.createDefault();
    }

    public static CloseableHttpClient createTrustAll() {
        return createDefault();
    }

    public static CloseableHttpClient createSSL(KeyStoreConfig keyStoreConfig, TrustStoreConfig trustStoreConfig)
            throws GeneralSecurityException, IOException {
        return createSSL(keyStoreConfig, trustStoreConfig, "SSL");
    }

    public static CloseableHttpClient createSSL(KeyStoreConfig keyStoreConfig, TrustStoreConfig trustStoreConfig, String sslType)
            throws GeneralSecurityException, IOException {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        ConnectionSocketFactory sslFactory = getConnectionSocketFactory(keyStoreConfig, trustStoreConfig, sslType);
        registryBuilder.register("https", sslFactory);

        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registryBuilder.build());
        pool.setMaxTotal(100);
        pool.setDefaultMaxPerRoute(25);

        return HttpClients.custom().setConnectionManager(pool).build();
    }

    public static CloseableHttpClient createSSLFromHost(String host) {
        return createSSLFromHost(host, null, null);
    }

    public static CloseableHttpClient createSSLFromHost(String host,
                                                        Function<Path, KeyStoreConfig> keyStoreMapping,
                                                        Function<Path, TrustStoreConfig> trustStoreMapping) {
        return createSSLFromHost(host, keyStoreMapping, trustStoreMapping, "SSL");
    }

    public static CloseableHttpClient createSSLFromHost(String host,
                                                        Function<Path, KeyStoreConfig> keyStoreMapping,
                                                        Function<Path, TrustStoreConfig> trustStoreMapping,
                                                        String sslType) {
        return HTTP_CLIENT_MAP.computeIfAbsent(host, i -> {
            EnvironmentConfig environmentConfig = Environment.getEnv(i);
            if (environmentConfig == null) {
                LOGGER.error("no environment of " + host);
                return createTrustAll();
            }

            return createCloseableHttpClient(environmentConfig, keyStoreMapping, trustStoreMapping, sslType);
        });
    }

    public static CloseableHttpClient createSSLFromEnvironment(@NotNull EnvironmentConfig environmentConfig) {
        return createSSLFromEnvironment(environmentConfig, null, null);
    }

    public static CloseableHttpClient createSSLFromEnvironment(@NotNull EnvironmentConfig environmentConfig,
                                                               Function<Path, KeyStoreConfig> keyStoreMapping,
                                                               Function<Path, TrustStoreConfig> trustStoreMapping) {
        return createSSLFromEnvironment(environmentConfig, keyStoreMapping, trustStoreMapping, "SSL");
    }

    public static CloseableHttpClient createSSLFromEnvironment(@NotNull EnvironmentConfig environmentConfig,
                                                               Function<Path, KeyStoreConfig> keyStoreMapping,
                                                               Function<Path, TrustStoreConfig> trustStoreMapping,
                                                               String sslType) {
        return HTTP_CLIENT_MAP.computeIfAbsent(environmentConfig.getHost(), i -> createCloseableHttpClient(environmentConfig, keyStoreMapping, trustStoreMapping, sslType));
    }

    public static CloseableHttpClient createCloseableHttpClient(@NotNull EnvironmentConfig environmentConfig,
                                                                Function<Path, KeyStoreConfig> keyStoreMapping,
                                                                Function<Path, TrustStoreConfig> trustStoreMapping,
                                                                String sslType) {
        try {
            Path path = new Environment(environmentConfig).download();
            KeyStoreConfig keyStore = Optional.ofNullable(keyStoreMapping)
                    .orElse(p -> null)
                    .apply(path);
            TrustStoreConfig trustStore = Optional.ofNullable(trustStoreMapping)
                    .orElse(p -> null)
                    .apply(path);
            return createSSL(keyStore, trustStore, sslType);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error(e.getMessage());
            return createTrustAll();
        }
    }

    private static ConnectionSocketFactory getConnectionSocketFactory(KeyStoreConfig keyStoreConfig,
                                                                      TrustStoreConfig trustStoreConfig,
                                                                      String sslType)
            throws GeneralSecurityException, IOException {
        KeyManager[] keyManagers = getKeyManagers(keyStoreConfig);
        TrustManager[] trustManagers = getTrustManagers(trustStoreConfig);

        SSLContext sslContext = SSLContext.getInstance(sslType);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return new SSLConnectionSocketFactory(sslContext, SUPPORTED_PROTOCOLS, SUPPORTED_CIPHER_SUITES, VERIFIERS.getHostnameVerifier());
    }

    private static KeyManager[] getKeyManagers(KeyStoreConfig keyStoreConfig)
            throws GeneralSecurityException, IOException {
        if (keyStoreConfig == null) {
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(keyStoreConfig.getStoreType());
        keyStore.load(openStream(keyStoreConfig.getPath()), keyStoreConfig.getStorePass().toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStoreConfig.getKeyPass().toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] getTrustManagers(TrustStoreConfig trustStoreConfig)
            throws GeneralSecurityException, IOException {
        if (trustStoreConfig == null) {
            return null;
        }

        KeyStore trustStore = KeyStore.getInstance(trustStoreConfig.getStoreType());
        trustStore.load(openStream(trustStoreConfig.getPath()), trustStoreConfig.getStorePass().toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static InputStream openStream(Path path) {
        try {
            return path.toUri().toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
