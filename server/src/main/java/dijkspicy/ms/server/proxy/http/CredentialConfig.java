package dijkspicy.ms.server.proxy.http;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Lookup;

/**
 * CredentialConfig
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class CredentialConfig {
    private CredentialsProvider credentialsProvider;
    private Lookup<AuthSchemeProvider> authRegistry;
    private AuthCache authCache;

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public CredentialConfig setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public Lookup<AuthSchemeProvider> getAuthRegistry() {
        return authRegistry;
    }

    public CredentialConfig setAuthRegistry(Lookup<AuthSchemeProvider> authRegistry) {
        this.authRegistry = authRegistry;
        return this;
    }

    public AuthCache getAuthCache() {
        return authCache;
    }

    public CredentialConfig setAuthCache(AuthCache authCache) {
        this.authCache = authCache;
        return this;
    }
}
