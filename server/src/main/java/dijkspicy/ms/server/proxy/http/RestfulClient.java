package dijkspicy.ms.server.proxy.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import dijkspicy.ms.server.common.Timer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
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

    public RestfulClient setCredentials(String user, String pass) {
        return this.setCredentials(AuthSchemesEnum.BASIC, user, pass);
    }

    public RestfulClient setCredentials(AuthSchemesEnum auth, String user, String pass) {
        Credentials credentials = new UsernamePasswordCredentials(user, pass);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

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
    public <T> T post(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback) {
        HttpPost req = new HttpPost();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T put(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback) {
        HttpPut req = new HttpPut();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T patch(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback) {
        HttpPatch req = new HttpPatch();
        req.setEntity(new ByteArrayEntity(request));
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T delete(String uri, Map<String, String> headers, Function<byte[], T> callback) {
        HttpDelete req = new HttpDelete();
        return this.send(req, uri, headers, callback);
    }

    @Override
    public <T> T get(String uri, Map<String, String> headers, Function<byte[], T> callback) {
        HttpGet req = new HttpGet();
        return this.send(req, uri, headers, callback);
    }

    public RestfulClient setEnvironment(RemoteEnvironment environment) {
        HttpClientFactory.addEnv(environment);
        return this;
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

    private <T> T send(HttpRequestBase req, String uri, Map<String, String> headers, Function<byte[], T> callback) {
        byte[] resp = this.send(req, uri, headers);
        return callback.apply(resp);
    }

    private byte[] send(HttpRequestBase req, String uri, Map<String, String> headers) {
        Optional.ofNullable(headers).ifPresent(it -> it.forEach(req::setHeader));
        req.setURI(URI.create(uri));

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
                 final AutoCloseable ignored = Timer.start(this)) {
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

    private CloseableHttpResponse execute(HttpUriRequest request, HttpClientContext context) throws IOException {
        URI uri = request.getURI();
        String schema = uri.getScheme();
        String host = uri.getHost();
        CloseableHttpClient closeableHttpClient = "https".equals(schema) && StringUtils.isNoneBlank(host)
                ? HttpClientFactory.createOrGet(host)
                : HttpClientFactory.createTrustAll();
        return closeableHttpClient.execute(request, context);
    }
}
