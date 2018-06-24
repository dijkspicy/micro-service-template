package dijkspicy.ms.server.proxy.http;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

/**
 * HostnameVerifierEnum
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum HostnameVerifierEnum {
    /**
     * default hostname verifier
     */
    STRICT(SSLConnectionSocketFactory.getDefaultHostnameVerifier()),
    /**
     * noop hostname verifier
     */
    NONE(NoopHostnameVerifier.INSTANCE),;

    private final HostnameVerifier hostnameVerifier;

    HostnameVerifierEnum(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }
}
