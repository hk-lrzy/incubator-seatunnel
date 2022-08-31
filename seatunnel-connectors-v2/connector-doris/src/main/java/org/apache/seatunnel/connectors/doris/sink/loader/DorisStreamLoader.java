package org.apache.seatunnel.connectors.doris.sink.loader;

import static org.apache.seatunnel.connectors.doris.common.DorisConstants.DORIS_SUCCESS_STATUS;

import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.connectors.doris.common.DorisConstants;
import org.apache.seatunnel.connectors.doris.common.DorisOptions;
import org.apache.seatunnel.connectors.doris.common.HttpClient;
import org.apache.seatunnel.connectors.doris.model.response.ResponseBody;
import org.apache.seatunnel.connectors.doris.sink.DorisLoader;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stream load
 * Created 2022/8/01
 */
public class DorisStreamLoader implements DorisLoader<String> {
    private static final Logger LOG = LoggerFactory.getLogger(DorisStreamLoader.class);

    private final DorisOptions dorisOptions;
    private final String loadUrl;
    private final NameValuePair[] parameters;
    private final Header header;
    private final HttpClient httpClient;

    public DorisStreamLoader(DorisOptions dorisOptions) {
        this.dorisOptions = dorisOptions;
        this.loadUrl = buildLoadUrl(dorisOptions);
        this.parameters = buildParameter(dorisOptions.getParameters());
        this.header = buildHeader(dorisOptions);
        this.httpClient = new HttpClient(dorisOptions);
    }

    @Override
    public void load(String request) {
        String formatDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String label = String.format("flink_sink_%s_%s", formatDate,
            UUID.randomUUID().toString().replaceAll("-", ""));
        HttpUriRequest putRequest = RequestBuilder
            .put(loadUrl)
            .addHeader(header)
            .addHeader(HttpHeaders.EXPECT, "100-continue")
            .addHeader("label", label)
            .addHeader("strip_outer_array", "true")
            .addHeader("format", "json")
            .setCharset(Charset.defaultCharset())
            .addParameters(parameters)
            .setEntity(new StringEntity(request, ContentType.TEXT_PLAIN))
            .build();

        String entity = httpClient.executeAndGetEntity(putRequest, true);
        ResponseBody responseBody = JsonUtils.parseObject(entity, new TypeReference<ResponseBody>() {
        });
        if (Objects.isNull(responseBody)) {
            throw new RuntimeException("Empty response.");

        } else {
            if (!DORIS_SUCCESS_STATUS.contains(responseBody.getStatus())
                || responseBody.getNumberTotalRows() != responseBody.getNumberLoadedRows()) {
                String errMsg = String.format("stream load error: %s, see more in %s",
                    responseBody.getMessage(), responseBody.getErrorURL());
                throw new RuntimeException(errMsg);
            }
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    private static NameValuePair[] buildParameter(Properties properties) {
        if (Objects.isNull(properties)) {
            return new NameValuePair[] {};
        }
        return properties.keySet()
            .stream()
            .map(key -> new BasicNameValuePair((String) key, properties.getProperty((String) key)))
            .collect(Collectors.toList()).toArray(new NameValuePair[] {});
    }

    private static Header buildHeader(DorisOptions dorisOptions) {
        String username = dorisOptions.getUsername();
        String password = dorisOptions.getPassword();
        String token = Base64.getEncoder().encodeToString(String.format("%s:%s", username, password)
            .getBytes(StandardCharsets.UTF_8));
        return new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + token);
    }

    private static String buildLoadUrl(DorisOptions dorisOptions) {
        String address = dorisOptions.getFeAddresses();
        String database = dorisOptions.getDatabaseName();
        String table = dorisOptions.getTableName();
        return String.format(DorisConstants.LOAD_URL_TEMPLATE, address, database, table);
    }
}
