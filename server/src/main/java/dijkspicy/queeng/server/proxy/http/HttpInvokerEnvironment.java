package dijkspicy.queeng.server.proxy.http;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public class HttpInvokerEnvironment {
    // Some basic exposed configurations
    private static final String MAX_POOLED_CONNECTION_PER_ROUTE_KEY = "odae.queeng.pooled.connections.per.route";
    private static final String MAX_POOLED_CONNECTION_PER_ROUTE_DEFAULT = "25";
    private static final String MAX_POOLED_CONNECTIONS_KEY = "odae.queeng.pooled.connections.max";
    private static final String MAX_POOLED_CONNECTIONS_DEFAULT = "100";

    private final String[] supportedProtocols = {
            "TLSv1",
            "TLSv1.1",
            "TLSv1.2"
    };
    private final String[] supportedCipherSuites = {
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA"
    };
    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    private HostnameVerifier hostnameVerifier;
    private CipherManager cipherManager = CipherManager.DEFAULT;
    private String sslType = "SSL";

    private HttpInvokerEnvironment() {
    }

    public static HttpInvokerEnvironment create() {
        return new HttpInvokerEnvironment();
    }

    public HttpInvokerEnvironment setCipherManager(CipherManager cipherManager) {
        this.cipherManager = cipherManager;
        return this;
    }

    public HttpInvokerEnvironment setTrustStore(Path trustStorePath, String storeType, String storePass) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.checkFile(trustStorePath, "Trust store file");

        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        KeyStore trustStore = KeyStore.getInstance(storeType);
        trustStore.load(trustStorePath.toUri().toURL().openStream(), this.cipherManager.decode(storePass).toCharArray());

        this.trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        this.trustManagerFactory.init(trustStore);
        return this;
    }

    public HttpInvokerEnvironment setTrustStore(Path trustStorePath, String storePass) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        return this.setTrustStore(trustStorePath, "jks", storePass);
    }

    public HttpInvokerEnvironment setKeyStore(Path keyStorePath, String storeType, String keyPass, String storePass) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        this.checkFile(keyStorePath, "Key store file");

        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        keyStore.load(keyStorePath.toUri().toURL().openStream(), this.cipherManager.decode(storePass).toCharArray());

        this.keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        this.keyManagerFactory.init(keyStore, this.cipherManager.decode(keyPass).toCharArray());
        return this;
    }

    public HttpInvokerEnvironment setKeyStore(Path keyStorePath, String keyPass, String storePass) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return this.setKeyStore(keyStorePath, "pkcs12", keyPass, storePass);
    }

    public HttpInvokerEnvironment setSslType(String sslType) {
        this.sslType = sslType;
        return this;
    }

    public HttpInvokerEnvironment setHostnameVerifier(HostnameVerifierEnum hostnameVerifier) {
        this.hostnameVerifier = Optional.ofNullable(hostnameVerifier)
                .orElse(HostnameVerifierEnum.NONE)
                .getHostnameVerifier();
        return this;
    }

    public HttpInvoker newInvoker() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance(this.sslType);
        KeyManager[] keyManagers = null;
        if (this.keyManagerFactory != null) {
            keyManagers = this.keyManagerFactory.getKeyManagers();
        }
        TrustManager[] trustManagers = null;
        if (this.trustManagerFactory != null) {
            trustManagers = this.trustManagerFactory.getTrustManagers();
        }

        SSLConnectionSocketFactory sslFactory = null;
        if (keyManagers != null || trustManagers != null) {
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            sslFactory = new SSLConnectionSocketFactory(sslContext, this.supportedProtocols, this.supportedCipherSuites, this.hostnameVerifier);
        }

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        if (null != sslFactory) {
            registryBuilder.register("https", sslFactory);
        }

        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registryBuilder.build());
        // Increase max total connection to 100
        final String maxCnxns = System.getProperty(MAX_POOLED_CONNECTIONS_KEY, MAX_POOLED_CONNECTIONS_DEFAULT);
        pool.setMaxTotal(Integer.parseInt(maxCnxns));

        // Increase default max connection per route to 25
        final String maxCnxnsPerRoute = System.getProperty(MAX_POOLED_CONNECTION_PER_ROUTE_KEY, MAX_POOLED_CONNECTION_PER_ROUTE_DEFAULT);
        pool.setDefaultMaxPerRoute(Integer.parseInt(maxCnxnsPerRoute));

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(pool).build();
        return new HttpInvoker(httpClient);
    }

    private void checkFile(Path trustStorePath, String fileName) {
        if (!Files.exists(trustStorePath)) {
            throw new RuntimeException(fileName + " not exists: " + trustStorePath);
        }
        if (!Files.isRegularFile(trustStorePath)) {
            throw new RuntimeException(fileName + " is not a file: " + trustStorePath);
        }
        if (!Files.isReadable(trustStorePath)) {
            throw new RuntimeException(fileName + " can't read: " + trustStorePath);
        }
    }
}
