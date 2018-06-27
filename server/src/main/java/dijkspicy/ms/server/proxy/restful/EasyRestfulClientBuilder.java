package dijkspicy.ms.server.proxy.restful;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * EasyRestfulClientBuilder
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class EasyRestfulClientBuilder {
    private String protocol;
    private String host;
    private int port;
    private String url;
    private Map<String, String> queries = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();

    private AuthSchemes authSchemes = AuthSchemes.BASIC;
    private String account;
    private String password;

    private RequestConfig requestConfig;

    public static void main(String[] args) throws MalformedURLException {
        new URL("http:///ads/ad/f/ads/f");
    }

    public final EasyRestfulClientBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public final EasyRestfulClientBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public final EasyRestfulClientBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public final EasyRestfulClientBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public final EasyRestfulClientBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public final EasyRestfulClientBuilder setQueries(Map<String, String> queries) {
        this.queries = queries;
        return this;
    }

    public final EasyRestfulClientBuilder setAccount(AuthSchemes authSchemes, String account, String password) {
        this.authSchemes = authSchemes;
        this.account = account;
        this.password = password;
        return this;
    }

    public final EasyRestfulClientBuilder setAccount(String account, String password) {
        return this.setAccount(AuthSchemes.BASIC, account, password);
    }

    public final EasyRestfulClientBuilder setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public final EasyRestfulClient build() {
        EasyRestfulClient restfulClient = new EasyRestfulClient(this.getHttpClient());
        this.initWithAccount(restfulClient);
        this.initWithURI(restfulClient);
        return restfulClient
                .setHeaders(this.headers)
                .setRequestConfig(this.requestConfig);
    }

    protected CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private void initWithURI(EasyRestfulClient restfulClient) {
        URI uri = URI.create(this.url);
        if (this.queries != null && !this.queries.isEmpty()) {
            StringJoiner joiner = new StringJoiner("&");
            this.queries.forEach((k, v) -> joiner.add(k + "=" + v));
            String query = uri.getQuery();
            if (query == null) {
                query = "?" + joiner;
            } else if (query.trim().isEmpty()) {
                query = joiner.toString();
            } else {
                query += "&" + joiner;
            }
            uri = uri.resolve(query);
        }
        restfulClient.setURI(uri);
    }

    private void initWithAccount(EasyRestfulClient restfulClient) {
        Credentials credentials = new UsernamePasswordCredentials(Objects.requireNonNull(account), Objects.requireNonNull(password));
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        RegistryBuilder<AuthSchemeProvider> authRegistryBuilder = RegistryBuilder.create();
        Optional.ofNullable(authSchemes)
                .orElse(AuthSchemes.BASIC)
                .register(authRegistryBuilder);
        Registry<AuthSchemeProvider> authRegistry = authRegistryBuilder.build();
        BasicAuthCache authCache = new BasicAuthCache();
        restfulClient
                .setBasicCredentialsProvider(credentialsProvider)
                .setLookup(authRegistry)
                .setAuthCache(authCache);
    }
}