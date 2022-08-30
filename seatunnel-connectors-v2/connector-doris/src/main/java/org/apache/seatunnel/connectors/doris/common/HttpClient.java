package org.apache.seatunnel.connectors.doris.common;

import com.google.common.io.Closeables;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created 2022/8/01
 */
public class HttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    private CloseableHttpClient client;
    private PoolingHttpClientConnectionManager connectionManager;

    public HttpClient(DorisOptions dorisOptions) {

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(dorisOptions.getHttpMaxTotal());
        connectionManager.setDefaultMaxPerRoute(dorisOptions.getHttpPerRoute());

        RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectionRequestTimeout(dorisOptions.getHttpRequestTimeout())
            .setConnectTimeout(dorisOptions.getHttpConnectTimeout())
            .setSocketTimeout(dorisOptions.getHttpWaitTimeout())
            .setRedirectsEnabled(true)
            .build();

        this.client = HttpClients
            .custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .setRedirectStrategy(new LaxRedirectStrategy())
            .setRetryHandler(DefaultHttpRequestRetryHandler.INSTANCE)
            .build();
    }

    public CloseableHttpResponse execute(HttpUriRequest uriRequest) {
        try {
            return this.client.execute(uriRequest);
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }

    public String executeAndGetEntity(HttpUriRequest uriRequest, boolean throwExceptionIfError) {
        Long start = System.currentTimeMillis();
        CloseableHttpResponse response = null;
        try {
            response = execute(uriRequest);
            if (throwExceptionIfError && isNotOk(response)) {
                LOG.warn("The request path is :[{}], request method is [{}], STATUS CODE = [{}]", uriRequest.getURI(), uriRequest.getMethod(), response.getStatusLine().getStatusCode());
                throw new IllegalStateException("Response Code is Not 200, Request ERROR");
            }
            return entityToString(response.getEntity());
        } catch (Exception e) {
            LOG.warn("Execute and get entity failed : [{}].", e.getMessage(), e);
            throw e;
        } finally {
            closeQuietly(response);
            Long end = System.currentTimeMillis();
            LOG.info("url: [{}] , cost time: [{}].", uriRequest.getURI(), end - start);
        }
    }

    public static String entityToString(HttpEntity entity) {
        try {
            if (entity == null) {
                LOG.warn("Response Code is 200, Response Entity Is Null");
                return null;
            } else {
                return formatEntity(EntityUtils.toString(entity));
            }
        } catch (Exception var2) {
            LOG.error("entity to string failed.", var2);
            throw new RuntimeException(var2);
        }
    }

    public static String formatEntity(String body) {
        if (body.startsWith("\"")) {
            body = body.substring(1);
        }

        if (body.endsWith("\"")) {
            body = body.substring(0, body.length() - 1);
        }

        return body;
    }

    public static boolean isOk(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode < HttpStatus.SC_BAD_REQUEST;
    }

    public static boolean isNotOk(HttpResponse response) {
        return !isOk(response);
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            Closeables.close(closeable, true);
        } catch (IOException var2) {
            LOG.error("IOException should not have been thrown.", var2);
        }
    }

    public void close() throws IOException {
        closeQuietly(this.client);
        closeQuietly(this.connectionManager);
    }

}
