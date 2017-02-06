package com.xz.scorep.executor.db;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * (description)
 * created at 16/12/28
 *
 * @author yidin
 */
@Component
public class DbiHandleFactoryManager {

    private Map<DbType, DbiHandleFactory> factoryMap = new HashMap<>();

    public void register(DbType dbType, DbiHandleFactory factory) {
        this.factoryMap.put(dbType, factory);
    }

    public DbiHandleFactory getDefaultDbiHandleFactory() {
        return this.factoryMap.values().stream()
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public DbiHandleFactory getDbiHandleFactory(DbType dbType) {
        return this.factoryMap.get(dbType);
    }
}
