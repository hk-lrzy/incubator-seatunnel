package org.apache.seatunnel.connectors.doris.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created 2022/8/24
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasicResponse {
    private int status;
    private String respMsg;
    private ResponseBody respContent;

}
