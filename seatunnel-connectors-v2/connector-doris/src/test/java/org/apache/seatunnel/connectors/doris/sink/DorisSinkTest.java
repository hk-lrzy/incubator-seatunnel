package org.apache.seatunnel.connectors.doris.sink;

import org.apache.seatunnel.core.starter.exception.CommandException;
import org.apache.seatunnel.e2e.flink.local.FlinkLocalContainer;

import org.junit.jupiter.api.Test;

/**
 * Created 2022/8/31
 */
public class DorisSinkTest {

    @Test
    public void testDorisSink() throws CommandException {
        FlinkLocalContainer localContainer = FlinkLocalContainer.builder().build();
        localContainer.executeSeaTunnelFlinkJob("doris/fake_to_doris.conf");
    }
}
