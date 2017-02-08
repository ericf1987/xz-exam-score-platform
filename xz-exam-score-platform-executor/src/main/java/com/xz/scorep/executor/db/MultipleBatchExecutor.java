package com.xz.scorep.executor.db;

import com.hyd.dao.DAO;

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

    private DAO dao;

    private int batchSize = DEFAULT_BATCH_SIZE;

    private Map<String, List<Map<String, Object>>> tableRowListMap = new HashMap<>();

    public MultipleBatchExecutor(DAO dao, int batchSize) {
        this.dao = dao;
        this.batchSize = batchSize;
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

        List<Map<String, Object>> rows = tableRowListMap.get(table);

        if (rows != null && !rows.isEmpty()) {
            dao.insert(rows, table);
            rows.clear();
        }
    }

    public void finish() {
        tableRowListMap.keySet().forEach(this::flush);
    }
}
