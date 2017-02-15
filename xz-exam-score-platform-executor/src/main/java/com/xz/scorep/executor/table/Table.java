package com.xz.scorep.executor.table;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class Table {

    private String key;

    private List<TableRow> rows = new ArrayList<>();

    private static final NaturalOrderComparator NAME_COMPARATOR = new NaturalOrderComparator();

    public Table(String key) {
        this.key = key;
    }

    public void setValue(String keyId, String propertyName, Object propertyValue) {

        for (TableRow row : rows) {
            if (keyId.equals(row.get(key))) {
                row.put(propertyName, propertyValue);
                return;
            }
        }

        TableRow tableRow = new TableRow(key, keyId);
        this.rows.add(tableRow);
        tableRow.put(propertyName, propertyValue);
    }

    public void readRow(Row row) {
        String keyId = row.getString(key);
        if (keyId == null) {
            throw new IllegalArgumentException("Row must contain key '" + key + "'");
        }

        row.keySet().stream()
                .filter(k -> !k.equals(key))
                .forEach(k -> setValue(keyId, k, row.get(k)));
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void sortBy(String columnName) {
        this.rows.sort((o1, o2) -> NAME_COMPARATOR.compare(
                String.valueOf(o1.get(columnName)),
                String.valueOf(o2.get(columnName))));
    }
}
