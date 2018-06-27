package dijkspicy.ms.server.proxy.restful;

import java.util.function.Supplier;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;

/**
 * AuthSchemes
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum AuthSchemes {
    /**
     * basic auth scheme
     */
    BASIC(org.apache.http.client.config.AuthSchemes.BASIC, BasicSchemeFactory::new),
    /**
     * digest auth scheme
     */
    DIGEST(org.apache.http.client.config.AuthSchemes.DIGEST, DigestSchemeFactory::new);

    private final String authType;
    private final Supplier<AuthSchemeProvider> schemeProvider;

    AuthSchemes(String authType, Supplier<AuthSchemeProvider> schemeProvider) {
        this.authType = authType;
        this.schemeProvider = schemeProvider;
    }

    public void register(RegistryBuilder<AuthSchemeProvider> builder) {
        builder.register(this.authType, this.schemeProvider.get());
    }
}
