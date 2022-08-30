package org.apache.seatunnel.connectors.doris.sink;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created 2022/8/01
 */
public interface DorisLoader<T> extends Serializable {

    void load(T t);

    void close() throws IOException;
}
