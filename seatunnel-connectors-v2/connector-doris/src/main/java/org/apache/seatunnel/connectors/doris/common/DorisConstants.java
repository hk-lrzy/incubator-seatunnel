package org.apache.seatunnel.connectors.doris.common;

import java.util.Arrays;
import java.util.List;

/**
 * Created 2022/8/24
 *
 */
public class DorisConstants {

    public static final String LOAD_URL_TEMPLATE = "http://%s/api/%s/%s/_stream_load";

    public static final List<String> DORIS_SUCCESS_STATUS = Arrays.asList("Success", "Publish Timeout");

}
