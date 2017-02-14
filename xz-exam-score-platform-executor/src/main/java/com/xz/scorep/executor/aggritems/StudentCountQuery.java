package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudentCountQuery {

    @Autowired
    private DAOFactory daoFactory;

    public int getSchoolStudentCount(String projectId, String schoolId) {
        String sql = "select count(1) from student, class where student.class_id=class.id and class.school_id=?";
        return daoFactory.getProjectDao(projectId).count(sql, schoolId);
    }

    public Map<String, Integer> getClassStudentCount(String projectId, String schoolId) {
        String sql = "select count(1) as cnt, class.id as class_id " +
                "  from student, class " +
                "  where student.class_id=class.id and class.school_id=?" +
                "  group by class.id";

        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql, schoolId);
        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("class_id"),
                row -> row.getInteger("cnt", 0)
        ));
    }


}
