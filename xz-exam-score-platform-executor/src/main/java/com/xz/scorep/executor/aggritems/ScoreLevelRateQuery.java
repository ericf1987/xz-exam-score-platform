package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.report.Keys.Range;
import com.xz.ajiaedu.common.report.Keys.Target;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class ScoreLevelRateQuery {

    public static final String CLASS_SLRS = "select * \n" +
            "from \n" +
            "  scorelevelmap\n" +
            "where \n" +
            "  target_type= ? and \n" +
            "  target_id  = ? and \n" +
            "  range_type = ? and \n" +
            "  range_id   in (\n" +
            "    select id from class where school_id=?\n" +
            "  )";

    @Autowired
    private DAOFactory daoFactory;

    // 查询学校总分四率
    public Row getSchoolProjectSLR(String projectId, String schoolId) {
        return getSchoolSLR(projectId, schoolId, Target.Project, projectId);
    }

    // 查询班级总分四率
    public List<Row> getClassProjectSLRs(String projectId, String schoolId) {
        return getClassSLRs(projectId, schoolId, Target.Project, projectId);
    }

    //////////////////////////////////////////////////////////////

    // 查询班级四率
    private List<Row> getClassSLRs(
            String projectId, String schoolId, Target target, String targetId) {

        List<Row> rows = daoFactory.getProjectDao(projectId).query(
                CLASS_SLRS, target.name(), targetId, Range.Class.name(), schoolId);

        // 竖表转横表
        Map<String, Row> resultRowMap = rows.stream().collect(Collectors.groupingBy(
                row -> row.getString("range_id"),
                Collector.of(
                        Row::new,
                        (combinedRow, row) -> combinedRow.put(
                                row.getString("score_level").toLowerCase(),
                                row.getDouble("student_rate", 0)
                        ),
                        (combinedRow1, combinedRow2) -> {
                            Row combined = new Row();
                            combined.putAll(combinedRow1);
                            combined.putAll(combinedRow2);
                            return combined;
                        },
                        Collector.Characteristics.IDENTITY_FINISH
                )
        ));

        // 补完 class_id 属性
        resultRowMap.entrySet().forEach(entry -> entry.getValue().put("class_id", entry.getKey()));

        return new ArrayList<>(resultRowMap.values());
    }

    // 查询学校四率
    private Row getSchoolSLR(String projectId, String schoolId, Target target, String targetId) {

        List<Row> rows = daoFactory.getProjectDao(projectId).query(
                CLASS_SLRS, Range.School.name(), schoolId, target.name(), targetId);

        Row result = new Row();
        result.put("school_id", schoolId);

        rows.forEach(row -> result.put(
                row.getString("score_level").toLowerCase(),
                row.getDouble("student_rate", 0)
        ));

        return result;
    }
}
