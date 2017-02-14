package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.MinMax;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MinMaxQuery {

    public static final String SCHOOL_MINMAX = "select \n" +
            "  min(score) as `min`,\n" +
            "  max(score) as `max`\n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and " +
            "  student.class_id=class.id and " +
            "  class.school_id=?";

    public static final String CLASS_MINMAX = "select \n" +
            "  class.id as class_id,\n" +
            "  min(score.score) as `min`,\n" +
            "  max(score.score) as `max`\n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and \n" +
            "  student.class_id=class.id and \n" +
            "  class.school_id=?\n" +
            "group by class.id";

    @Autowired
    private DAOFactory daoFactory;

    public MinMax getSchoolProjectMinMax(String projectId, String schoolId) {
        String sql = SCHOOL_MINMAX.replace("{{table}}", "score_project");
        Row row = daoFactory.getProjectDao(projectId).queryFirst(sql, schoolId);
        return row2MinMax(row);
    }

    private MinMax row2MinMax(Row row) {
        return new MinMax(row.getDouble("min", 0), row.getDouble("max", 0));
    }

    public Map<String, MinMax> getClassProjectMinMax(String projectId, String schoolId) {
        String sql = CLASS_MINMAX.replace("{{table}}", "score_project");
        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql, schoolId);

        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("class_id"),
                this::row2MinMax
        ));
    }
}
