package org.apache.seatunnel.e2e.flink.v2.doris;

import org.apache.seatunnel.e2e.flink.FlinkContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container;

import java.io.IOException;

public class FakeSourceToDorisIT extends FlinkContainer {

  @Test
  public void testFakeSourceToLocalFileText() throws IOException, InterruptedException {
    Container.ExecResult execResult = executeSeaTunnelFlinkJob("/doris/fake_to_doris.conf");
    Assertions.assertEquals(0, execResult.getExitCode());
  }
}
