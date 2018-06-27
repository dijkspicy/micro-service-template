package dijkspicy.ms.server.proxy.restful;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * EasyRestfulSafeClientBuilder
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class SSLClientBuilder extends SimpleClientBuilder {
    private static final int MAX_POOLED_CONNECTION_PER_ROUTE_DEFAULT = 25;
    private static final int MAX_POOLED_CONNECTIONS_DEFAULT = 100;

    private HostnameVerifiers hostnameVerifier;
    private CipherManager cipherManager = CipherManager.DEFAULT;
    private String sslType = "SSL";
    private TrustStoreInfo trustStore;
    private KeyStoreInfo keyStore;

    public final SSLClientBuilder setCipherManager(CipherManager cipherManager) {
        this.cipherManager = cipherManager;
        return this;
    }

    public final SSLClientBuilder setTrustStore(Path trustStorePath, String storeType, String storePass) throws RestfulException {
        this.checkFile(trustStorePath, "Trust store file");
        this.trustStore = new TrustStoreInfo(trustStorePath, storeType, storePass);
        return this;
    }

    public final SSLClientBuilder setTrustStore(Path trustStorePath, String storePass) throws RestfulException {
        return this.setTrustStore(trustStorePath, "jks", storePass);
    }

    public final SSLClientBuilder setKeyStore(Path keyStorePath, String storeType, String keyPass, String storePass) throws RestfulException {
        this.checkFile(keyStorePath, "Key store file");
        this.keyStore = new KeyStoreInfo(keyStorePath, storeType, keyPass, storePass);
        return this;
    }

    public final SSLClientBuilder setKeyStore(Path keyStorePath, String keyPass, String storePass) throws RestfulException {
        return this.setKeyStore(keyStorePath, "pkcs12", keyPass, storePass);
    }

    public final SSLClientBuilder setSslType(String sslType) {
        this.sslType = sslType;
        return this;
    }

    public final SSLClientBuilder setHostnameVerifier(HostnameVerifiers hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    protected CloseableHttpClient getHttpClient() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());

        HostnameVerifier verifier = Optional.ofNullable(this.hostnameVerifier)
                .orElse(HostnameVerifiers.NONE)
                .getHostnameVerifier();
        try {
            ConnectionSocketFactory sslFactory = new SSLFactory(this.trustStore, this.keyStore, this.sslType, verifier).init();
            registryBuilder.register("https", sslFactory);
        } catch (GeneralSecurityException | IOException e) {
            throw new RestfulException("Failed to init ssl context: " + e.getMessage(), e);
        }

        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registryBuilder.build());
        // Increase max total connection to 100
        pool.setMaxTotal(MAX_POOLED_CONNECTIONS_DEFAULT);

        // Increase default max connection per route to 25
        pool.setDefaultMaxPerRoute(MAX_POOLED_CONNECTION_PER_ROUTE_DEFAULT);

        return HttpClients.custom().setConnectionManager(pool).build();
    }

    private static InputStream openStream(Path trustStorePath) {
        try {
            return trustStorePath.toUri().toURL().openStream();
        } catch (IOException e) {
            throw new RestfulException("Failed to read file: " + trustStorePath + " due to " + e.getMessage(), e);
        }
    }

    private void checkFile(Path trustStorePath, String fileName) {
        if (!Files.exists(trustStorePath)) {
            throw new RestfulException(fileName + " not exists: " + trustStorePath);
        }
        if (!Files.isRegularFile(trustStorePath)) {
            throw new RestfulException(fileName + " is not a file: " + trustStorePath);
        }
        if (!Files.isReadable(trustStorePath)) {
            throw new RestfulException(fileName + " can't read: " + trustStorePath);
        }
    }

    private char[] decode(String pass) {
        return this.cipherManager.decode(pass).toCharArray();
    }

    class TrustStoreInfo {
        private final Path trustStorePath;
        private final String storeType;
        private final String storePass;

        TrustStoreInfo(Path trustStorePath, String storeType, String storePass) {
            this.trustStorePath = trustStorePath;
            this.storeType = storeType;
            this.storePass = storePass;
        }

        TrustManagerFactory init() throws GeneralSecurityException, IOException {
            KeyStore trustStore = KeyStore.getInstance(this.storeType);
            trustStore.load(openStream(this.trustStorePath), decode(this.storePass));

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        }
    }

    class KeyStoreInfo {
        private final Path keyStorePath;
        private final String storeType;
        private final String keyPass;
        private final String storePass;

        KeyStoreInfo(Path keyStorePath, String storeType, String keyPass, String storePass) {
            this.keyStorePath = keyStorePath;
            this.storeType = storeType;
            this.keyPass = keyPass;
            this.storePass = storePass;
        }

        KeyManagerFactory init() throws GeneralSecurityException, IOException {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(openStream(keyStorePath), decode(storePass));

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, decode(keyPass));
            return keyManagerFactory;
        }
    }

    class SSLFactory {
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
        private TrustStoreInfo trustStore;
        private KeyStoreInfo keyStore;
        private String sslType;
        private HostnameVerifier hostnameVerifier;

        SSLFactory(TrustStoreInfo trustStore, KeyStoreInfo keyStore, String sslType, HostnameVerifier hostnameVerifier) {
            this.trustStore = trustStore;
            this.keyStore = keyStore;
            this.sslType = sslType;
            this.hostnameVerifier = hostnameVerifier;
        }

        ConnectionSocketFactory init() throws GeneralSecurityException, IOException {
            KeyManager[] keyManagers = null;
            if (this.keyStore != null) {
                keyManagers = this.keyStore.init().getKeyManagers();
            }
            TrustManager[] trustManagers = null;
            if (this.trustStore != null) {
                trustManagers = this.trustStore.init().getTrustManagers();
            }

            SSLContext sslContext = SSLContext.getInstance(this.sslType);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            return new SSLConnectionSocketFactory(sslContext, this.supportedProtocols, this.supportedCipherSuites, this.hostnameVerifier);
        }
    }
}
