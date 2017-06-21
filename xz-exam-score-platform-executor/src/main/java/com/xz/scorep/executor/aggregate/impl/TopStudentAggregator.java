package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 尖子生统计(含班级,学校尖子生列表,班级,学校尖子生比例和人数)
 *
 * @author luckylo
 * @createTime 2017-06-12.
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(71)
public class TopStudentAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(TopStudentAggregator.class);

    //查省维度的尖子生最低分
    private static final String QUERY_PROVINCE_SCORE = "" +
            "select (\n" +
            " select b.score from \n" +
            "   (select (@row := @row +1) num,s.* from score_project s,(select @row :=0) a ORDER BY score desc) b \n" +
            "   where b.num = (select floor(COUNT(1) * {{rate}}) from score_project)\n" +
            " ) score\n";

    //根据省维度的尖子生最低分获取省维度的尖子生列表
    private static final String QUERY_PROVINCE_TOP_STUDENT_LIST = "select * from score_project  where score >= {{score}}  order by score desc";

    //查学校维度的尖子生最低分
    private static final String QUERY_SCHOOL_SCORE = "select c.score from (\n" +
            " select (@row  :=@row+1) num,a.* from (\n" +
            "   select score.student_id,score.score,student.class_id,student.school_id\n" +
            "   from score_project score,student \n" +
            "   where score.student_id = student.id \n" +
            "   and student.school_id = '{{schoolId}}'\n" +
            "   order by score desc) a,\n" +
            "   (select @row :=0)b\n" +
            ") c  \n" +
            "where \n" +
            "c.num = (\n" +
            " select floor(count(1) * {{rate}}) as count from score_project score,student \n" +
            " where score.student_id = student.id \n" +
            " and student.school_id = '{{schoolId}}'\n" +
            ")";

    //根据学校维度的尖子生最低分查学校维度的尖子生列表
    private static final String QUERY_SCHOOL_TOP_STUDENT_LIST = "select score.student_id,score.score," +
            "student.class_id,student.school_id\n" +
            "from score_project score,student \n" +
            "where \n" +
            "score.score >= {{score}} \n" +
            "and score.student_id = student.id \n" +
            "and student.school_id = '{{schoolId}}'\n" +
            "order by score desc";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        double rate = reportConfig.getTopStudentRate();

        projectDao.execute("truncate table top_student_rate");
        projectDao.execute("truncate table top_student_list");

        LOG.info("开始统计项目ID {} 的学生尖子生比例......", projectId);

        aggregateSchoolTopStudentRate(projectId, projectDao, rate);
        aggregateClassTopStudentRate(projectId, projectDao, rate);
        LOG.info("开始统计项目ID {} 的学生尖子生比例......", projectId);
    }

    private void aggregateClassTopStudentRate(String projectId, DAO projectDao, double rate) {
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<Map<String, Object>> topStudentRateMap = new ArrayList<>();
        List<Map<String, Object>> topStudentListMap = new ArrayList<>();

        for (ProjectSchool school : schools) {
            String schoolId = school.getId();
            String rateSql = QUERY_SCHOOL_SCORE
                    .replace("{{schoolId}}", schoolId)
                    .replace("{{rate}}", String.valueOf(rate));
            double score = projectDao.queryFirst(rateSql).getDouble("score", 0);

            String sql = QUERY_SCHOOL_TOP_STUDENT_LIST
                    .replace("{{schoolId}}", schoolId)
                    .replace("{{score}}", String.valueOf(score));
            List<Row> schoolTopStudentList = projectDao.query(sql);
            List<ProjectClass> classes = classService.listClasses(projectId, schoolId);
            classes.forEach(clazz -> {
                String classId = clazz.getId();
                if (schoolTopStudentList.isEmpty()) {
                    topStudentRateMap.add(createMap("Class", classId, "Project", projectId, 1, null));
                    return;
                }
                List<Row> classTopStudentList = schoolTopStudentList.stream()
                        .filter(row -> classId.equals(row.getString("class_id")))
                        .collect(Collectors.toList());
                topStudentRateMap.add(createMap("Class", classId, "Project", projectId, schoolTopStudentList.size(), classTopStudentList));
                List<Map<String, Object>> listMap = createListMap("Class", classId, "Project", projectId, classTopStudentList);
                if (!listMap.isEmpty()) {
                    topStudentListMap.addAll(listMap);
                }

            });

        }
        projectDao.insert(topStudentRateMap, "top_student_rate");
        projectDao.insert(topStudentListMap, "top_student_list");
    }

    private void aggregateSchoolTopStudentRate(String projectId, DAO projectDao, double rate) {
        List<Row> students = projectDao.query("select * from student");
        Row scoreRow = projectDao.queryFirst(QUERY_PROVINCE_SCORE.replace("{{rate}}", String.valueOf(rate)));
        String score = scoreRow.getString("score");

        List<Row> provinceTopStudents = projectDao.query(QUERY_PROVINCE_TOP_STUDENT_LIST.replace("{{score}}", score));

        int totalCount = provinceTopStudents.size();
        List<ProjectSchool> schools = schoolService.listSchool(projectId);

        List<Map<String, Object>> topStudentRateMap = new ArrayList<>();
        List<Map<String, Object>> topStudentListMap = new ArrayList<>();
        schools.forEach(school -> {
            String schoolId = school.getId();

            List<String> schoolStudents = students.stream()
                    .filter(row -> schoolId.equals(row.getString("school_id")))
                    .map(row -> row.getString("id"))
                    .collect(Collectors.toList());

            List<Row> schoolStudentScore = provinceTopStudents.stream()
                    .filter(row -> schoolStudents.contains(row.getString("student_id")))
                    .collect(Collectors.toList());

            topStudentRateMap.add(createMap("School", schoolId, "Project", projectId, totalCount, schoolStudentScore));

            List<Map<String, Object>> listMap = createListMap("School", schoolId, "Project", projectId, schoolStudentScore);
            if (!listMap.isEmpty()) {
                topStudentListMap.addAll(listMap);
            }
        });
        projectDao.insert(topStudentRateMap, "top_student_rate");
        projectDao.insert(topStudentListMap, "top_student_list");

    }

    private Map<String, Object> createMap(String rangeType, String rangeId, String targetType, String targetId, int totalCount, List<Row> scoreRows) {
        Map<String, Object> map = new HashMap<>();
        int count = scoreRows.isEmpty() ? 0 : scoreRows.size();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("target_type", targetType);
        map.put("target_id", targetId);
        map.put("count", count);
        map.put("rate", (count * 1.0 / totalCount));
        return map;
    }


    private List<Map<String, Object>> createListMap(String rangeType, String rangeId, String targetType, String targetId, List<Row> rows) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (rows.isEmpty()) {
            return list;
        }

        for (Row row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("range_type", rangeType);
            map.put("range_id", rangeId);
            map.put("target_type", targetType);
            map.put("target_id", targetId);
            map.put("student_id", row.getString("student_id"));
            list.add(map);
        }
        return list;
    }
}

