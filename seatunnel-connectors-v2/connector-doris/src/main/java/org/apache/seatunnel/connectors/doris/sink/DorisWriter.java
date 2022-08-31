package org.apache.seatunnel.connectors.doris.sink;

import org.apache.seatunnel.api.sink.SinkWriter;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.connectors.doris.common.DorisOptions;
import org.apache.seatunnel.connectors.doris.sink.loader.DorisStreamLoader;
import org.apache.seatunnel.connectors.seatunnel.common.sink.AbstractSinkWriter;
import org.apache.seatunnel.format.json.JsonSerializationSchema;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 2022/8/01
 */
public class DorisWriter extends AbstractSinkWriter<SeaTunnelRow, Void> {
    private static final Logger LOG = LoggerFactory.getLogger(DorisWriter.class);

    private final SeaTunnelRowType seaTunnelRowType;
    private final DorisOptions options;
    private final JsonSerializationSchema serializationSchema;
    private final DorisLoader<String> loader;

    private final List<JsonNode> batch;

    public DorisWriter(Config dorisSinkConf,
                       SeaTunnelRowType seaTunnelRowType,
                       SinkWriter.Context context) {
        this.seaTunnelRowType = seaTunnelRowType;
        this.options = DorisOptions.fromPluginConfig(dorisSinkConf);
        this.serializationSchema = new JsonSerializationSchema(seaTunnelRowType);
        //now we only support stream load, maybe future broker load will implement in seatunnel.
        this.loader = new DorisStreamLoader(options);
        this.batch = new ArrayList<>();
        LOG.info("Subtask {} create writer.", context.getIndexOfSubtask());
    }

    @Override
    public void write(SeaTunnelRow element) throws IOException {
        batch.add(JsonUtils.stringToJsonNode(new String(serializationSchema.serialize(element))));
        if (options.getBatchSize() > 0 && batch.size() >= options.getBatchSize()) {
            flush();
        }
    }

    private synchronized void flush() {
        String request = JsonUtils.toJsonString(batch);
        loader.load(request);
        batch.clear();
    }

    @Override
    public void close() throws IOException {
        flush();
        loader.close();
    }
}
