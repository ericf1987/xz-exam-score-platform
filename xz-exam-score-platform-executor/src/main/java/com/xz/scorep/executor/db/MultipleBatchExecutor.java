package com.xz.scorep.executor.db;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.ajiaedu.common.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
public class MultipleBatchExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleBatchExecutor.class);

    private static final int DEFAULT_BATCH_SIZE = 500;

    private static final int POOL_SIZE = 10;

    private static final int QUEUE_SIZE = 10;

    private DAO dao;

    private int batchSize = DEFAULT_BATCH_SIZE;

    private boolean hasInsertedData;

    private Map<String, List<Object>> tableRowListMap = new HashMap<>();

    private ExecutorService pool;

    public MultipleBatchExecutor(DAO dao, int batchSize) {
        this.dao = dao;
        this.batchSize = batchSize;
    }

    public void push(String table, Object object) {

        ensureMapElement(table);

        List<Object> rowList = tableRowListMap.get(table);
        rowList.add(object);

        if (rowList.size() >= batchSize) {
            flush(table);
        }
    }

    private void ensureMapElement(String table) {
        if (!tableRowListMap.containsKey(table)) {
            tableRowListMap.put(table, new ArrayList<>());
        }
    }

    private void flush(String table) {

        if (this.pool == null) {
            this.pool = Executors.newBlockingThreadPoolExecutor(POOL_SIZE, POOL_SIZE, QUEUE_SIZE);
        }

        List<Object> rows = tableRowListMap.get(table);

        if (rows != null && !rows.isEmpty()) {
            this.hasInsertedData = true;

            this.pool.submit(() -> {
                try {
                    dao.insert(rows, table);
                } catch (DAOException e) {
                    LOG.error("批量插入到 " + table + " 失败", e);
                }
            });
            tableRowListMap.put(table, new ArrayList<>());
        }
    }

    public boolean hasInsertedData() {
        return this.hasInsertedData;
    }

    public void finish() {
        clearTableRowListMap();
        shutdownThreadPool();
    }

    private void clearTableRowListMap() {
        this.tableRowListMap.keySet().forEach(this::flush);
    }

    private void shutdownThreadPool() {
        try {
            if (this.pool != null) {
                this.pool.shutdown();
                this.pool.awaitTermination(1, TimeUnit.DAYS);
            }
        } catch (InterruptedException e) {
            throw new BatchExecutorException(e);
        }
    }
}
