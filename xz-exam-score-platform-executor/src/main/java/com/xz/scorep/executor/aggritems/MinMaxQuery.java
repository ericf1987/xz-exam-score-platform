package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MinMaxQuery {

    private static final String SCHOOL_MINMAX = "select \n" +
            "  min(score) as `min`,\n" +
            "  max(score) as `max`\n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and " +
            "  student.class_id=class.id and " +
            "  class.school_id=?";

    private static final String CLASS_MINMAX = "select \n" +
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

    public Row getSchoolProjectMinMax(String projectId, String schoolId) {
        String sql = SCHOOL_MINMAX.replace("{{table}}", "score_project");
        Row row = daoFactory.getProjectDao(projectId).queryFirst(sql, schoolId);
        row.put("school_id", schoolId);
        return row;
    }

    public List<Row> getClassProjectMinMax(String projectId, String schoolId) {
        String sql = CLASS_MINMAX.replace("{{table}}", "score_project");
        return daoFactory.getProjectDao(projectId).query(sql, schoolId);
    }
}
