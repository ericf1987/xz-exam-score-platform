package com.xz.scorep.executor.exportexcel.impl.subject;

import com.hyd.dao.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: luckylo
 * Date : 2017-05-04
 */
public class Row2MapHelper {

    public static List<Map<String, Object>> row2Map(List<Row> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }).collect(Collectors.toList());
    }

}
