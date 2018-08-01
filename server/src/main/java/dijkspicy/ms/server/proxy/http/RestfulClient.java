package dijkspicy.ms.server.proxy.http;

import dijkspicy.ms.base.Timer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * RestfulClient
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class RestfulClient implements Restful {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulClient.class);
    private CredentialConfig credentialConfig;
    private RequestConfig requestConfig;
    private CloseableHttpClient httpClient;

    public RestfulClient() {
    }

    public RestfulClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private static boolean isUnavailable(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_UNAVAILABLE;
    }

    private static boolean isServerError(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    private static boolean isSuccess(int statusCode) {
        return 200 <= statusCode && statusCode <= 299;
    }

    public RestfulClient setCredentials(String user, String pass) {
        return this.setCredentials(AuthSchemesEnum.BASIC, user, pass);
    }

    public RestfulClient setCredentials(AuthSchemesEnum auth, String user, String pass) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));

        RegistryBuilder<AuthSchemeProvider> authRegistryBuilder = RegistryBuilder.create();
        Optional.ofNullable(auth)
                .orElse(AuthSchemesEnum.BASIC)
                .register(authRegistryBuilder);
        Registry<AuthSchemeProvider> authRegistry = authRegistryBuilder.build();
        BasicAuthCache authCache = new BasicAuthCache();

        this.credentialConfig = new CredentialConfig()
                .setCredentialsProvider(credentialsProvider)
                .setAuthRegistry(authRegistry)
                .setAuthCache(authCache);
        return this;
    }

    public RestfulClient setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    @Override
    public <T> T post(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        HttpPost req = new HttpPost();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T put(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        HttpPut req = new HttpPut();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T patch(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        HttpPatch req = new HttpPatch();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T delete(String uri, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        HttpDelete req = new HttpDelete();
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T get(String uri, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        HttpGet req = new HttpGet();
        return this.send(req, uri, headers, callback);
    }

    protected CloseableHttpClient getHttpClient() {
        return HttpClientFactory.createTrustAll();
    }

    protected CloseableHttpClient getHttpsClient(String host) {
        return HttpClientFactory.createSSLFromHost(host);
    }

    private <T> T send(HttpRequestBase req, String uri, Map<String, String> headers, Function<RestfulClientCallback, T> callback) {
        req.setURI(URI.create(uri));

        byte[] resp = this.send(req, headers);
        RestfulClientCallback restfulClientCallback = new RestfulClientCallback(
                req.getMethod(),
                req.getURI().toString(),
                headers,
                resp
        );
        return callback.apply(restfulClientCallback);
    }

    private byte[] send(HttpRequestBase req, Map<String, String> headers) {
        Optional.ofNullable(headers).ifPresent(it -> it.forEach(req::setHeader));

        HttpClientContext context = HttpClientContext.create();
        Optional.ofNullable(this.requestConfig)
                .ifPresent(context::setRequestConfig);
        Optional.ofNullable(this.credentialConfig)
                .ifPresent(it -> {
                    context.setAuthCache(it.getAuthCache());
                    context.setCredentialsProvider(it.getCredentialsProvider());
                    context.setAuthSchemeRegistry(it.getAuthRegistry());
                });

        do {
            try (final CloseableHttpResponse response = this.execute(req, context);
                 final AutoCloseable ignored = Timer.start(req)) {
                final int statusCode = response.getStatusLine().getStatusCode();
                if (isSuccess(statusCode) || isServerError(statusCode)) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else if (isUnavailable(statusCode)) {
                    LOGGER.debug("Failed to connect wrap server (HTTP/503), retrying");
                    continue;
                }

                String responseContent = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("Failed to execute HTTP Request, got HTTP/" + statusCode + ", content: " + responseContent);
            } catch (NoHttpResponseException e) {
                // This can happen when sitting behind a load balancer and a backend server dies
                LOGGER.debug("The server failed to issue an HTTP response, retrying");
            } catch (Exception e) {
                LOGGER.debug("Failed to execute HTTP request", e);
                throw new RuntimeException(e);
            }
        } while (true);
    }

    private synchronized CloseableHttpResponse execute(HttpUriRequest request, HttpClientContext context) throws IOException {
        URI uri = request.getURI();
        String schema = uri.getScheme();
        String host = uri.getHost();
        return Optional.ofNullable(this.httpClient)
                .orElseGet(() -> {
                    boolean needCertificate = "https".equals(schema) && StringUtils.isNoneBlank(host)
                            && !"127.0.0.1".equals(host) && !"localhost".equals(host) && !"0.0.0.0".equals(host);
                    return this.httpClient = needCertificate
                            ? this.getHttpsClient(host)
                            : this.getHttpClient();
                })
                .execute(request, context);
    }
}
