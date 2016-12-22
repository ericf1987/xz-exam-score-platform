package com.xz.scorep.executor.db;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.function.Consumer;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
public class DBIHandle {

    private DBI dbi;

    public DBIHandle(DBI dbi) {
        this.dbi = dbi;
    }

    public void runHandle(Consumer<Handle> handleConsumer) {
        try (Handle handle = this.dbi.open()) {
            handleConsumer.accept(handle);
        }
    }
}
