package dijkspicy.ms.server.proxy.restful;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

/**
 * HostnameVerifiers
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum HostnameVerifiers {
    /**
     * default hostname verifier
     */
    STRICT(SSLConnectionSocketFactory.getDefaultHostnameVerifier()),
    /**
     * noop hostname verifier
     */
    NONE(NoopHostnameVerifier.INSTANCE),;

    private final HostnameVerifier hostnameVerifier;

    HostnameVerifiers(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }
}
