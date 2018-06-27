package dijkspicy.ms.server.proxy.restful;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Optional;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * EasyRestfulSafeClientBuilder
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class EasyRestfulSafeClientBuilder extends EasyRestfulClientBuilder {
    private static final int MAX_POOLED_CONNECTION_PER_ROUTE_DEFAULT = 25;
    private static final int MAX_POOLED_CONNECTIONS_DEFAULT = 100;

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

    public final EasyRestfulSafeClientBuilder setCipherManager(CipherManager cipherManager) {
        this.cipherManager = cipherManager;
        return this;
    }

    public final EasyRestfulSafeClientBuilder setTrustStore(Path trustStorePath, String storeType, String storePass) throws RestfulException {
        this.checkFile(trustStorePath, "Trust store file");

        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        try {
            KeyStore trustStore = KeyStore.getInstance(storeType);
            trustStore.load(openStream(trustStorePath), this.cipherManager.decode(storePass).toCharArray());
            this.trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
            this.trustManagerFactory.init(trustStore);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RestfulException("Failed to set trust store file: " + e.getMessage(), e);
        }
        return this;
    }

    public final EasyRestfulSafeClientBuilder setTrustStore(Path trustStorePath, String storePass) throws RestfulException {
        return this.setTrustStore(trustStorePath, "jks", storePass);
    }

    public final EasyRestfulSafeClientBuilder setKeyStore(Path keyStorePath, String storeType, String keyPass, String storePass) throws RestfulException {
        this.checkFile(keyStorePath, "Key store file");

        try {
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(openStream(keyStorePath), this.cipherManager.decode(storePass).toCharArray());

            this.keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            this.keyManagerFactory.init(keyStore, this.cipherManager.decode(keyPass).toCharArray());
        } catch (KeyStoreException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RestfulException("Failed to set key store file: " + e.getMessage(), e);
        }
        return this;
    }

    public final EasyRestfulSafeClientBuilder setKeyStore(Path keyStorePath, String keyPass, String storePass) throws RestfulException {
        return this.setKeyStore(keyStorePath, "pkcs12", keyPass, storePass);
    }

    public final EasyRestfulSafeClientBuilder setSslType(String sslType) {
        this.sslType = sslType;
        return this;
    }

    public final EasyRestfulSafeClientBuilder setHostnameVerifier(HostnameVerifiers hostnameVerifier) {
        this.hostnameVerifier = Optional.ofNullable(hostnameVerifier)
                .orElse(HostnameVerifiers.NONE)
                .getHostnameVerifier();
        return this;
    }

    @Override
    protected CloseableHttpClient getHttpClient() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(this.sslType);
        } catch (NoSuchAlgorithmException e) {
            throw new RestfulException("No such algorithm: " + e.getMessage(), e);
        }

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
            try {
                sslContext.init(keyManagers, trustManagers, new SecureRandom());
            } catch (KeyManagementException e) {
                throw new RestfulException("Key management error: " + e.getMessage(), e);
            }
            sslFactory = new SSLConnectionSocketFactory(sslContext, this.supportedProtocols, this.supportedCipherSuites, this.hostnameVerifier);
        }

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        if (null != sslFactory) {
            registryBuilder.register("https", sslFactory);
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
}
