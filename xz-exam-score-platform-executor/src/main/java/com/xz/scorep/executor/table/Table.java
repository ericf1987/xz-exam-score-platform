package com.xz.scorep.executor.table;

import com.hyd.dao.Row;
import com.hyd.dao.util.StringUtil;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class Table {

    private String key;

    private List<TableRow> rows = new ArrayList<>();

    private Map<String, Integer> columnIndexes = new HashMap<>();

    private static final NaturalOrderComparator NAME_COMPARATOR = new NaturalOrderComparator();

    public Table() {
    }

    public Table(String key) {
        this.key = key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setColumnIndex(int index, String columnName) {
        this.columnIndexes.put(columnName, index);
    }

    public int getColumnIndex(String columnName) {
        if (!columnIndexes.containsKey(columnName)) {
            return -1;
        } else {
            return columnIndexes.get(columnName);
        }
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

        if (StringUtil.isEmpty(this.key)) {
            throw new IllegalStateException("没有设置 Table 的 key");
        }

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

    public void sortBy(String... columnNames) {
        this.rows.sort((o1, o2) -> {
            for (String columnName : columnNames) {
                int result = NAME_COMPARATOR.compare(
                        String.valueOf(o1.get(columnName)),
                        String.valueOf(o2.get(columnName)));

                if (result != 0) {
                    return result;
                }
            }

            return 0;
        });
    }

    public void readRows(List<Row> rows) {
        rows.forEach(this::readRow);
    }

    public void setColumnNames(int startIndex, String... columnNames) {
        IntStream.range(startIndex, startIndex + columnNames.length).forEach(index -> {
            String columnName = columnNames[index - startIndex];
            setColumnIndex(index, columnName);
        });
    }
}
