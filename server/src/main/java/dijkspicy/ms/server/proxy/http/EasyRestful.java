package dijkspicy.ms.server.proxy.http;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import dijkspicy.ms.server.common.Timer;
import dijkspicy.ms.server.proxy.restful.AuthSchemes;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EasyRestful
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public class EasyRestful {
    private static final Logger LOGGER = LoggerFactory.getLogger(EasyRestful.class);
    private final CloseableHttpClient httpClient;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> query;
    private CredentialsProvider credentialsProvider;
    private Lookup<AuthSchemeProvider> authRegistry;
    private AuthCache authCache;
    private HttpHost host;
    private String method;
    private URI uri;
    private RequestConfig requestConfig;

    public EasyRestful() {
        this(HttpClients.createDefault());
    }

    public EasyRestful(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public EasyRestful login(String username, String password) {
        return this.login(AuthSchemes.BASIC, username, password);
    }

    public EasyRestful login(AuthSchemes authType, String username, String password) {
        Credentials credentials = new UsernamePasswordCredentials(Objects.requireNonNull(username), Objects.requireNonNull(password));
        this.credentialsProvider = new BasicCredentialsProvider();
        this.credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        RegistryBuilder<AuthSchemeProvider> authRegistryBuilder = RegistryBuilder.create();
        Optional.ofNullable(authType)
                .orElse(AuthSchemes.BASIC)
                .register(authRegistryBuilder);
        this.authRegistry = authRegistryBuilder.build();
        this.authCache = new BasicAuthCache();
        return this;
    }

    public EasyRestful invoke(String url) {
        this.url = url;
        this.resetHost();
        return this;
    }

    public EasyRestful withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public EasyRestful withQuery(Map<String, String> query) {
        this.query = query;
        this.resetHost();
        return this;
    }

    public EasyRestful withRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public byte[] post(byte[] request) {
        return this.post(request, out -> out);
    }

    public <T> T post(byte[] request, Function<byte[], T> callback) {
        this.method = HttpPost.METHOD_NAME;
        byte[] response = this.send(request);
        return callback.apply(response);
    }

    public byte[] put(byte[] request) {
        return this.put(request, out -> out);
    }

    public <T> T put(byte[] request, Function<byte[], T> callback) {
        this.method = HttpPut.METHOD_NAME;
        byte[] response = this.send(request);
        return callback.apply(response);
    }

    public byte[] patch(byte[] request) {
        return this.patch(request, out -> out);
    }

    public <T> T patch(byte[] request, Function<byte[], T> callback) {
        this.method = HttpPatch.METHOD_NAME;
        byte[] response = this.send(request);
        return callback.apply(response);
    }

    public byte[] delete() {
        return this.delete(out -> out);
    }

    public <T> T delete(Function<byte[], T> callback) {
        this.method = HttpDelete.METHOD_NAME;
        byte[] response = this.send(null);
        return callback.apply(response);
    }

    public byte[] get() {
        return this.get(out -> out);
    }

    public <T> T get(Function<byte[], T> callback) {
        this.method = HttpGet.METHOD_NAME;
        byte[] response = this.send(null);
        return callback.apply(response);
    }

    @Override
    public int hashCode() {

        return Objects.hash(method, url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EasyRestful that = (EasyRestful) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(url, that.url);
    }

    @Override
    public String toString() {
        return this.method + " " + this.url;
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

    private byte[] send(byte[] request) {
        this.initHost();

        HttpClientContext context = HttpClientContext.create();
        context.setTargetHost(this.host);

        // Set the credentials if they were provided.
        if (null != this.credentialsProvider) {
            context.setCredentialsProvider(this.credentialsProvider);
            context.setAuthSchemeRegistry(this.authRegistry);
            context.setAuthCache(this.authCache);
        }

        if (null != this.requestConfig) {
            context.setRequestConfig(this.requestConfig);
        }

        // http request
        HttpUriRequest httpUriRequest = this.genRequest(request);

        do {
            try (final CloseableHttpResponse response = this.execute(httpUriRequest, context);
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
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.debug("Failed to execute HTTP request", e);
                throw new RuntimeException(e);
            }
        } while (true);
    }

    private void resetHost() {
        this.uri = null;
        this.host = null;
    }

    private void initHost() {
        if (this.host != null && this.uri != null) {
            return;
        }

        try {
            URL tempUrl = this.genURL(this.url, this.query);
            this.uri = tempUrl.toURI();
            this.host = new HttpHost(tempUrl.getHost(), tempUrl.getPort(), tempUrl.getProtocol());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid url: " + url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid url string: " + url);
        }
    }

    private HttpUriRequest genRequest(byte[] rawBody) {
        HttpUriRequest request;
        switch (this.method) {
            case HttpPost.METHOD_NAME:
                HttpPost httpPost = new HttpPost(this.uri);
                httpPost.setEntity(new ByteArrayEntity(rawBody));
                request = httpPost;
                break;
            case HttpDelete.METHOD_NAME:
                request = new HttpDelete(this.uri);
                break;
            case HttpPut.METHOD_NAME:
                HttpPut httpPut = new HttpPut(this.uri);
                httpPut.setEntity(new ByteArrayEntity(rawBody));
                request = httpPut;
                break;
            case HttpGet.METHOD_NAME:
                request = new HttpGet(this.uri);
                break;
            case HttpPatch.METHOD_NAME:
                HttpPatch httpPatch = new HttpPatch(this.uri);
                httpPatch.setEntity(new ByteArrayEntity(rawBody));
                request = httpPatch;
                break;
            default:
                throw new RuntimeException("Invalid http method: " + this.method);
        }
        Optional.ofNullable(this.headers).ifPresent(it -> it.forEach(request::setHeader));
        return request;
    }

    private CloseableHttpResponse execute(HttpUriRequest post, HttpClientContext context) throws IOException {
        return this.httpClient.execute(post, context);
    }

    private URL genURL(String link, Map<String, String> queryParams) throws MalformedURLException {
        URL url = new URL(Objects.requireNonNull(link, "Http url can't be null"));
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((k, v) -> joiner.add(k + "=" + v));
        String query = url.getQuery();
        if (query == null) {
            query = "?" + joiner;
        } else if (query.trim().isEmpty()) {
            query = joiner.toString();
        } else {
            query += "&" + joiner;
        }
        return new URL(url + query);
    }
}
