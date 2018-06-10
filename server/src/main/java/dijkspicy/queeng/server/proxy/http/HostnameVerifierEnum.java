package dijkspicy.queeng.server.proxy.http;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.HostnameVerifier;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum HostnameVerifierEnum {
    STRICT(SSLConnectionSocketFactory.getDefaultHostnameVerifier()),
    NONE(NoopHostnameVerifier.INSTANCE),;

    private final HostnameVerifier hostnameVerifier;

    HostnameVerifierEnum(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }
}
