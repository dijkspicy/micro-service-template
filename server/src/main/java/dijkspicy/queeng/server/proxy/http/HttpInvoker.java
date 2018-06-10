package dijkspicy.queeng.server.proxy.http;

import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public class HttpInvoker {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpInvoker.class);
    private final CloseableHttpClient httpClient;
    // params
    private HttpMethod httpMethod;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> query;
    private CredentialsProvider credentialsProvider;
    private Lookup<AuthSchemeProvider> authRegistry;
    private AuthCache authCache;
    // host
    private HttpHost host;
    private URI uri;

    public HttpInvoker() {
        this(HttpClients.createDefault());
    }

    public HttpInvoker(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpInvoker login(String username, String password) {
        return this.login(AuthSchemesEnum.BASIC, username, password);
    }

    public HttpInvoker login(AuthSchemesEnum authType, String username, String password) {
        Credentials credentials = new UsernamePasswordCredentials(Objects.requireNonNull(username), Objects.requireNonNull(password));
        this.credentialsProvider = new BasicCredentialsProvider();
        this.credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        RegistryBuilder<AuthSchemeProvider> authRegistryBuilder = RegistryBuilder.create();
        Optional.ofNullable(authType)
                .orElse(AuthSchemesEnum.BASIC)
                .register(authRegistryBuilder);
        this.authRegistry = authRegistryBuilder.build();
        this.authCache = new BasicAuthCache();
        return this;
    }

    public HttpInvoker invoke(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.resetHost();
        return this;
    }

    public HttpInvoker withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpInvoker withQuery(Map<String, String> query) {
        this.query = query;
        this.resetHost();
        return this;
    }

    public <T> T send(byte[] request, Function<byte[], T> callback) {
        byte[] out = this.send(request);
        return callback.apply(out);
    }

    public byte[] send(byte[] request) {
        this.initHost();

        HttpClientContext context = HttpClientContext.create();
        context.setTargetHost(this.host);

        // Set the credentials if they were provided.
        if (null != this.credentialsProvider) {
            context.setCredentialsProvider(this.credentialsProvider);
            context.setAuthSchemeRegistry(this.authRegistry);
            context.setAuthCache(this.authCache);
        }

        // http request
        HttpUriRequest post = this.genRequest(request);

        while (true) {
            try (CloseableHttpResponse response = this.execute(post, context)) {
                final int statusCode = response.getStatusLine().getStatusCode();
                if (isSuccess(statusCode) || isServerError(statusCode)) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else if (isUnavailable(statusCode)) {
                    LOGGER.debug("Failed to connect to server (HTTP/503), retrying");
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
        }

    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpInvoker that = (HttpInvoker) o;
        return httpMethod == that.httpMethod && Objects.equals(uri, that.uri);
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
        switch (this.httpMethod) {
            case POST:
                HttpPost httpPost = new HttpPost(this.uri);
                httpPost.setEntity(new ByteArrayEntity(rawBody));
                request = httpPost;
                break;
            case DELETE:
                request = new HttpDelete(this.uri);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(this.uri);
                httpPut.setEntity(new ByteArrayEntity(rawBody));
                request = httpPut;
                break;
            case GET:
                request = new HttpGet(this.uri);
                break;
            case PATCH:
                HttpPatch httpPatch = new HttpPatch(this.uri);
                httpPatch.setEntity(new ByteArrayEntity(rawBody));
                request = httpPatch;
                break;
            default:
                throw new RuntimeException("Invalid http method: " + this.httpMethod);
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
