package dijkspicy.ms.server.proxy.restful;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import dijkspicy.ms.server.common.Timer;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.AuthCache;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EasyRestfulClient
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class EasyRestfulClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(EasyRestfulClient.class);
    private final CloseableHttpClient httpClient;

    private BasicCredentialsProvider credentialsProvider;
    private Lookup<AuthSchemeProvider> authRegistry;
    private AuthCache authCache;

    private HttpHost host;
    private String method;
    private URI uri;
    private Map<String, String> headers;

    private RequestConfig requestConfig;

    public EasyRestfulClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public EasyRestfulClient setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public EasyRestfulClient setBasicCredentialsProvider(BasicCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public EasyRestfulClient setLookup(Lookup<AuthSchemeProvider> authRegistry) {
        this.authRegistry = authRegistry;
        return this;
    }

    public EasyRestfulClient setAuthCache(AuthCache authCache) {
        this.authCache = authCache;
        return this;
    }

    public EasyRestfulClient setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public EasyRestfulClient setHttpHost(HttpHost host) {
        this.host = host;
        return this;
    }

    public EasyRestfulClient setHeaders(Map<String, String> headers) {
        this.headers = headers;
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
    public String toString() {
        return this.method + " " + this.uri;
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
}
