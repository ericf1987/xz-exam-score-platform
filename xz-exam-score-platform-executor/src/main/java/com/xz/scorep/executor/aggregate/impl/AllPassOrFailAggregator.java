package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-07
 */
@AggragateOrder(7)
@Component
public class AllPassOrFailAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(AllPassOrFailAggregator.class);

    public static String SQL_TEMP = "select " +
            " @total :=(select count(student.id) from student where student.{{key}} = '{{schoolId}}') as total," +
            " @pass_count :=({{passSql}}) as all_pass_count," +
            " @pass_count /@total as all_pass_rate," +
            " @fail_count :=({{failSql}}) as all_fail_count," +
            " @fail_count /@total as all_fail_rate";

    public static String SCHOOL_TEMP = "select count(student.id) from student,school {{tables}}" +
            " where student.{{key}} = school.id " +
            " and student.school_id = '{{schoolId}}' {{sub}} {{passOrFail}} ";

    public static String CLASS_TEMP = "select count(student.id) from student,class {{tables}}" +
            " where student.{{key}} = class.id " +
            " and student.school_id = '{{schoolId}}' {{sub}} {{passOrFail}} ";


    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;

    @Autowired
    ProjectService projectService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;


    @Override
    public void aggregate(String projectId) throws Exception {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table all_pass_or_fail ");
        LOG.info("全科及格率、不及格率表已清空");

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject scoreLevels = JSON.parseObject(reportConfig.getScoreLevels());

        List<Row> subjects = projectDao.query("select * from subject");

        LOG.info("统计学校全科及格率、全科不及格率...");
        aggregateSchoolRate(projectDao, projectId, scoreLevels, subjects);

        LOG.info("统计班级全科及格率、全科不及格率...");
        aggregateClassRate(projectDao, projectId, scoreLevels, subjects);

    }

    private void aggregateClassRate(DAO projectDao, String projectId, JSONObject scoreLevels, List<Row> subjects) {
        double passRate = scoreLevels.getDouble("Pass");

        List<Row> rows = new ArrayList<>();
        classService.listClasses(projectId).forEach(c -> {
            String sql = SQL_TEMP
                    .replace("{{passSql}}", getClassTempSql(subjects, passRate, true))
                    .replace("{{failSql}}", getClassTempSql(subjects, passRate, false))
                    .replace("{{key}}", "class_id")
                    .replace("{{schoolId}}", c.getId());
            Row row = projectDao.queryFirst(sql);
            row.put("rang_type", Range.CLASS);
            row.put("rang_id", c.getId());
            rows.add(row);
        });
        projectDao.insert(rows, "all_pass_or_fail");
    }

    private void aggregateSchoolRate(DAO projectDao, String projectId, JSONObject scoreLevels, List<Row> subjects) {
        double passRate = scoreLevels.getDouble("Pass");

        List<Row> rows = new ArrayList<>();
        schoolService.listSchool(projectId).forEach(school -> {
            String sql = SQL_TEMP
                    .replace("{{passSql}}", getSchoolTempSql(subjects, passRate, true))
                    .replace("{{failSql}}", getSchoolTempSql(subjects, passRate, false))
                    .replace("{{key}}", "school_id")
                    .replace("{{schoolId}}", school.getId());
            Row row = projectDao.queryFirst(sql);
            row.put("rang_type", Range.SCHOOL);
            row.put("rang_id", school.getId());
            rows.add(row);
        });
        projectDao.insert(rows, "all_pass_or_fail");
    }


    private String getSchoolTempSql(List<Row> subjects, double passRate, boolean pass) {
        StringBuffer tables = new StringBuffer();
        StringBuffer sub = new StringBuffer();
        StringBuffer passSub = new StringBuffer();
        StringBuffer failSub = new StringBuffer();
        for (Row row : subjects) {
            String id = row.getString("id");
            double score = row.getDouble("full_score", 0) * passRate;

            tables.append(", score_subject_" + id);
            sub.append(" AND score_subject_" + id + ".student_id = " + "student.id ");
            passSub.append(" AND score_subject_" + id + ".score >= " + score);
            failSub.append(" AND score_subject_" + id + ".score <= " + score);
        }
        if (pass) {
            return SCHOOL_TEMP
                    .replace("{{tables}}", tables.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", passSub.toString());
        } else {
            return SCHOOL_TEMP
                    .replace("{{tables}}", tables.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", failSub.toString());
        }
    }

    private String getClassTempSql(List<Row> subjects, double passRate, boolean pass) {
        StringBuffer tables = new StringBuffer();
        StringBuffer sub = new StringBuffer();
        StringBuffer passSub = new StringBuffer();
        StringBuffer failSub = new StringBuffer();
        for (Row row : subjects) {
            String id = row.getString("id");
            double score = row.getDouble("full_score", 0);

            tables.append(", score_subject_" + id);
            sub.append(" AND score_subject_" + id + ".student_id = " + "student.id ");
            passSub.append(" AND score_subject_" + id + ".score >= " + score * passRate);
            failSub.append(" AND score_subject_" + id + ".score <= " + score * passRate);
        }
        if (pass) {
            return CLASS_TEMP
                    .replace("{{tables}}", tables.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", passSub.toString());
        } else {
            return CLASS_TEMP
                    .replace("{{tables}}", tables.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", failSub.toString());
        }

    }
}
