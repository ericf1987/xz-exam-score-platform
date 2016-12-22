package com.xz.scorep.executor.db;

import org.skife.jdbi.v2.PreparedBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
public class MultipleBatchExecutor {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private int batchSize = DEFAULT_BATCH_SIZE;

    private DBIHandle dbiHandle;

    private Map<String, String> tableSqlMap = new HashMap<>();

    private Map<String, List<Map<String, Object>>> tableRowListMap = new HashMap<>();

    public MultipleBatchExecutor(DBIHandle dbiHandle) {
        this.dbiHandle = dbiHandle;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void prepareBatch(String table, String sql) {
        this.tableSqlMap.put(table, sql);
    }

    public void push(String table, Map<String, Object> row) {

        if (!tableRowListMap.containsKey(table)) {
            tableRowListMap.put(table, new ArrayList<>());
        }

        List<Map<String, Object>> rowList = tableRowListMap.get(table);
        rowList.add(row);

        if (rowList.size() >= batchSize) {
            flush(table);
        }
    }

    private void flush(String table) {
        System.out.println("Flushing table " + table);

        String sql = tableSqlMap.get(table);
        List<Map<String, Object>> rows = tableRowListMap.get(table);

        if (sql != null && rows != null && !rows.isEmpty()) {
            dbiHandle.runHandle(handle -> {
                PreparedBatch preparedBatch = handle.prepareBatch(sql);
                rows.forEach(preparedBatch::add);
                preparedBatch.execute();
            });
            rows.clear();
        }
    }

    public void finish() {
        tableSqlMap.keySet().forEach(this::flush);
    }
}
