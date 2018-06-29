package dijkspicy.ms.server.proxy.http;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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
    private static final String[] supportedProtocols = {
            "TLSv1",
            "TLSv1.1",
            "TLSv1.2"
    };
    private static final String[] supportedCipherSuites = {
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA"
    };

    public static void setEnvConfig() {

    }

    public static CloseableHttpClient createDefault() {
        return HttpClients.createDefault();
    }

    public static CloseableHttpClient createTrustAll() {
        return createDefault();
    }

    public static CloseableHttpClient createSSL(TrustStoreConfig trustStoreConfig, KeyStoreConfig keyStoreConfig) throws GeneralSecurityException, IOException {
        return createSSL(trustStoreConfig, keyStoreConfig, "SSL");
    }

    public static CloseableHttpClient createSSL(TrustStoreConfig trustStoreConfig, KeyStoreConfig keyStoreConfig, String sslType) throws GeneralSecurityException, IOException {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        ConnectionSocketFactory sslFactory = getConnectionSocketFactory(trustStoreConfig, keyStoreConfig, sslType);
        registryBuilder.register("https", sslFactory);

        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registryBuilder.build());
        pool.setMaxTotal(100);
        pool.setDefaultMaxPerRoute(25);

        return HttpClients.custom().setConnectionManager(pool).build();
    }

    public static CloseableHttpClient createOrGet(String host) {
        return HTTP_CLIENT_MAP.computeIfAbsent(host, i -> {
            EnvironmentConfig environmentConfig = Environment.getEnv(i);
            if (environmentConfig == null) {
                LOGGER.error("no environment of " + host);
                return createTrustAll();
            }

            try {
                Path path = new Environment(environmentConfig).download();
                KeyStoreConfig keyStoreConfig = new KeyStoreConfig()
                        .setPath(path.resolve("ssl/internal/server.p12"));
                TrustStoreConfig trustStoreConfig = new TrustStoreConfig()
                        .setPath(path.resolve("ssl/internal/trust.jks"));
                return createSSL(trustStoreConfig, keyStoreConfig);
            } catch (IOException | GeneralSecurityException e) {
                LOGGER.error(e.getMessage());
                return createTrustAll();
            }
        });
    }

    private static ConnectionSocketFactory getConnectionSocketFactory(TrustStoreConfig trustStoreConfig, KeyStoreConfig keyStoreConfig, String sslType) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        TrustManager[] trustManagers = getTrustManagers(trustStoreConfig);
        KeyManager[] keyManagers = getKeyManagers(keyStoreConfig);

        SSLContext sslContext = SSLContext.getInstance(sslType);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return new SSLConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, NoopHostnameVerifier.INSTANCE);
    }

    private static KeyManager[] getKeyManagers(KeyStoreConfig keyStoreConfig) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreConfig.getStoreType());
        keyStore.load(openStream(keyStoreConfig.getPath()), keyStoreConfig.getStorePass().toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStoreConfig.getKeyPass().toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] getTrustManagers(TrustStoreConfig trustStoreConfig) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
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
