package com.xz.scorep.executor.aggritems;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学校报表查询
 * Author: luckylo
 * Date : 2017-03-14
 */
@Component
public class SchoolDetailReportQuery {

    private static final String QUERY_SCHOOL_TOTAL_DETAIL = "select \n" +
            "a.class_name,a.student_count,a.max_score,'{{schoolId}}' as school_id,\n" +
            "a.min_score,a.avg_score,\n" +
            "CONCAT(IFNULL(xlnt.xlnt_rate,'0.00'),'%') as xlnt_rate,\n" +
            "CONCAT(IFNULL(good.good_rate,'0.00'),'%') as good_rate,\n" +
            "CONCAT(IFNULL(pass.pass_rate,'0.00'),'%') as pass_rate\n" +
            "from\n" +
            "(\n" +
            "select \n" +
            "'全校' as class_name,\n" +
            "count(student.id) as student_count,\n" +
            "max({{table}}.score) as max_score,\n" +
            "min({{table}}.score) as min_score,\n" +
            "avg({{table}}.score) as avg_score\n" +
            "from \n" +
            "student,{{table}}\n" +
            "where student.school_id = '{{schoolId}}'\n" +
            "and {{table}}.student_id = student.id\n" +
            ") a\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'全校' as class_name,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from scorelevelmap,school\n" +
            "WHERE\n" +
            "school.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.range_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_type = '{{targetType}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            ") xlnt on a.class_name = xlnt.class_name\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'全校' as class_name,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.range_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_type = '{{targetType}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            ") good on a.class_name = good.class_name\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'全校' as class_name,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.range_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_type = '{{targetType}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            ") pass on pass.class_name = a.class_name\n";

    private static final String QUERY_SCHOOL_CLASS_DETAIL = "select\n" +
            "a.class_name,\n" +
            "a.student_count,a.class_id,\n" +
            "a.max_score,\n" +
            "a.min_score,\n" +
            "a.avg_score,\n" +
            "CONCAT(IFNULL(xlnt.xlnt_rate,'0.00'),'%') as xlnt_rate,\n" +
            "CONCAT(IFNULL(good.good_rate,'0.00'),'%') as good_rate,\n" +
            "CONCAT(IFNULL(pass.pass_rate,'0.00'),'%') as pass_rate\n" +
            "FROM\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "class.name as class_name,\n" +
            "count(student.id) as student_count,\n" +
            "max({{table}}.score) as max_score,\n" +
            "min({{table}}.score) as min_score,\n" +
            "avg({{table}}.score) as avg_score\n" +
            "from \n" +
            "student,class,{{table}}\n" +
            "where \n" +
            "student.school_id = '{{schoolId}}'\n" +
            "and student.id = {{table}}.student_id\n" +
            "and student.class_id = class.id\n" +
            "GROUP BY class.id\n" +
            "ORDER BY class.name\n" +
            ") a \n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from \n" +
            "class,scorelevelmap\n" +
            "where\n" +
            "class.id = scorelevelmap.range_id\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            ")xlnt on xlnt.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from \n" +
            "class,scorelevelmap\n" +
            "where\n" +
            "class.id = scorelevelmap.range_id\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            ") good on good.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from \n" +
            "class,scorelevelmap\n" +
            "where\n" +
            "class.id = scorelevelmap.range_id\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.target_id = '{{targetId}}'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            ") pass on pass.class_id = a.class_id\n";

    @Autowired
    DAOFactory daoFactory;

    /**
     * 全校总分详细报表
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @return
     */
    public Row getSchoolSubjectsTotalDetail(String projectId, String schoolId) {
        String sql = QUERY_SCHOOL_TOTAL_DETAIL
                .replace("{{table}}", "score_project")
                .replace("{{targetType}}","Project")
                .replace("{{schoolId}}", schoolId)
                .replace("{{targetId}}", projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.queryFirst(sql);
    }

    /**
     * 班级总分详细报表
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @return
     */
    public List<Row> getClassSubjectsTotalDetail(String projectId, String schoolId) {
        String sql = QUERY_SCHOOL_CLASS_DETAIL
                .replace("{{table}}", "score_project")
                .replace("{{schoolId}}", schoolId)
                .replace("{{targetId}}", projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.query(sql);
    }


    /**
     * 全校单科详细报表
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @param subjectId 科目ID
     * @return
     */
    public Row getSchoolSubjectTotalDetail(String projectId, String schoolId, String subjectId) {
        String sql = QUERY_SCHOOL_TOTAL_DETAIL
                .replace("{{table}}", "score_subject_" + subjectId)
                .replace("{{targetType}}","Subject")
                .replace("{{schoolId}}", schoolId)
                .replace("{{targetId}}", subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.queryFirst(sql);
    }


    /**
     * 查询某个科目的班级详细报表
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @param subjectId 科目ID
     * @return
     */
    public List<Row> getClassSubjectTotalDetail(String projectId, String schoolId, String subjectId) {
        String sql = QUERY_SCHOOL_CLASS_DETAIL
                .replace("{{table}}", "score_subject_" + subjectId)
                .replace("{{schoolId}}", schoolId)
                .replace("{{targetId}}", subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.query(sql);
    }
}
