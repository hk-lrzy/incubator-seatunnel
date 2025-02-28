/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.file.sink.transaction;

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.connectors.seatunnel.file.sink.FileSinkState;

import lombok.NonNull;

import java.util.List;

public interface TransactionStateFileWriter extends Transaction {
    void write(@NonNull SeaTunnelRow seaTunnelRow);

    /**
     * In this method we need finish write the file. The following operations are often required:
     * 1. Flush memory to disk.
     * 2. Close output stream.
     * 3. Add the mapping relationship between seatunnel file path and hive file path to needMoveFiles.
     */
    void finishAndCloseWriteFile();

    /**
     * snapshotState
     * @param checkpointId checkpointId
     * @return
     */
    List<FileSinkState> snapshotState(long checkpointId);
}
