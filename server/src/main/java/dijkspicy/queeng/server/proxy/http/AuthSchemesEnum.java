package dijkspicy.queeng.server.proxy.http;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;

import java.util.function.Supplier;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum AuthSchemesEnum {
    BASIC(AuthSchemes.BASIC, BasicSchemeFactory::new),
    DIGEST(AuthSchemes.DIGEST, DigestSchemeFactory::new);

    private final String authType;
    private final Supplier<AuthSchemeProvider> schemeProvider;

    AuthSchemesEnum(String authType, Supplier<AuthSchemeProvider> schemeProvider) {
        this.authType = authType;
        this.schemeProvider = schemeProvider;
    }

    public void register(RegistryBuilder<AuthSchemeProvider> builder) {
        builder.register(this.authType, this.schemeProvider.get());
    }
}
