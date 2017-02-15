package com.xz.scorep.executor.table;

import java.util.HashMap;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class TableRow extends HashMap<String, Object> {

    public TableRow(String keyName, String keyId) {
        this.put(keyName, keyId);
    }
}
