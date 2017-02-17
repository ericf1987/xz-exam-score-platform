package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.hyd.dao.database.commandbuilder.Command;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudentQuery {

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

    public List<Row> listStudentInfo(String projectId, Range range) {

        String sql = "select " +
                "  student.id as student_id, " +
                "  student.exam_no as exam_no, " +
                "  student.school_exam_no as school_exam_no, " +
                "  student.name as student_name, " +
                "  class.name as class_name," +
                "  school.name as school_name," +
                "  school.area as area " +
                "from " +
                "  student, class, school " +
                "where " +
                "  student.class_id=class.id and" +
                "  class.school_id=school.id";

        List<Object> params = new ArrayList<>();

        if (range.match(Range.SCHOOL)) {
            sql += " and school.id=?";
            params.add(range.getId());
        } else if (range.match(Range.CLASS)) {
            sql += " and class.id=?";
            params.add(range.getId());
        }

        return daoFactory.getProjectDao(projectId).query(new Command(sql, params));
    }
}
