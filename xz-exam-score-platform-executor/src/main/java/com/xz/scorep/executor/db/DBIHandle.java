package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
public class DBIHandle {

    private DBI dbi;

    private DataSource dataSource;

    public DBIHandle(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dbi = new DBI(dataSource);
    }

    public void runHandle(Consumer<Handle> handleConsumer) {
        try (Handle handle = this.dbi.open()) {
            handleConsumer.accept(handle);
        }
    }

    public <T> T queryFirst(Function<Handle, T> queryFunction) {
        try (Handle handle = this.dbi.open()) {
            return queryFunction.apply(handle);
        }
    }

    public <T> List<T> queryList(Function<Handle, List<T>> queryFunction) {
        try (Handle handle = this.dbi.open()) {
            return queryFunction.apply(handle);
        }
    }

    public void close() {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
        this.dataSource = null;
    }
}
