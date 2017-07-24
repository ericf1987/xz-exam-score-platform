package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
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

/**
 * 高分段统计(总体学校,学校班级)
 *
 * @author luckylo
 * @createTime 2017-06-21.
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(72)
public class HighScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(HighScoreAggregator.class);

    public static final String QUERY_PROVINCE_DATA = "select avg(score) average from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.*  from (select * from `{{table}}` order by score desc) a,(select @row:=0) b \n" +
            "  ) c\n" +
            "  where num <= (\n" +
            "  select floor(COUNT(1) * {{rate}}) from `{{table}}`)";

    public static final String QUERY_SCHOOL_DATA = "select avg(score) average from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.* from \n" +
            "    (\n" +
            "      select score.student_id,score.score,student.school_id \n" +
            "      from `{{table}}` score,student\n" +
            "      where \n" +
            "      student.id = score.student_id \n" +
            "      and student.school_id = \"{{schoolId}}\"\n" +
            "      order by score.score desc\n" +
            "    ) a,(select @row :=0) b\n" +
            "  ) c\n" +
            "    where c.num <= \n" +
            "  (\n" +
            "    select floor(COUNT(1) * {{rate}}) from `{{table}}` score,student \n" +
            "    where student.id = score.student_id\n" +
            "    and student.school_id = \"{{schoolId}}\"\n" +
            "  );";

    public static final String QUERY_CLASS_DATA = "select avg(score) average from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.* from \n" +
            "    (\n" +
            "      select score.student_id,score.score,student.school_id \n" +
            "      from `{{table}}` score,student\n" +
            "      where \n" +
            "      student.id = score.student_id \n" +
            "      and student.class_id = \"{{classId}}\"\n" +
            "      order by score.score desc\n" +
            "    ) a,(select @row :=0) b\n" +
            "  ) c\n" +
            "    where c.num <= \n" +
            "  (\n" +
            "    select floor(COUNT(1) * {{rate}}) from `{{table}}` score,student \n" +
            "    where student.id = score.student_id\n" +
            "    and student.class_id = \"{{classId}}\"\n" +
            "  );";


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        double rate = reportConfig.getHighScoreRate();

        projectDao.execute("truncate table high_score");
        LOG.info("项目ID {} 开始统计高分段学生平均分....", projectId);

        aggregate0(projectId, projectDao, rate);

        LOG.info("项目ID {} 高分段学生平均分始统统计完成....", projectId);

    }

    private void aggregate0(String projectId, DAO projectDao, double rate) {
        aggregateProjectHighScore(projectId, projectDao, rate);
        aggregateSubjectHighScore(projectId, projectDao, rate);
        aggregateQuestHighScore(projectId, projectDao, rate);
    }

    private void aggregateProjectHighScore(String projectId, DAO projectDao, double rate) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        Row projectRow = projectDao.queryFirst(QUERY_PROVINCE_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", "score_project"));
        if (projectRow != null) {
            insertMap.add(createMap(Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.PROJECT, projectId, projectRow.getDouble("average", 0)));
        }

        String schoolTmp = QUERY_SCHOOL_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", "score_project");
        String classTmp = QUERY_CLASS_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", "score_project");
        schoolService.listSchool(projectId)
                .forEach(school -> {
                    String schoolId = school.getId();
                    Row row = projectDao.queryFirst(schoolTmp.replace("{{schoolId}}", schoolId));
                    if (row != null) {
                        insertMap.add(createMap(Range.SCHOOL, schoolId, Target.PROJECT, projectId, row.getDouble("average", 0)));
                    }
                });

        classService.listClasses(projectId)
                .forEach(clazz -> {
                    String classId = clazz.getId();
                    Row row = projectDao.queryFirst(classTmp.replace("{{classId}}", classId));
                    if (row != null) {
                        insertMap.add(createMap(Range.CLASS, classId, Target.PROJECT, projectId, row.getDouble("average", 0)));
                    }
                });

        projectDao.insert(insertMap, "high_score");
    }

    private void aggregateSubjectHighScore(String projectId, DAO projectDao, double rate) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        subjectService.listSubjects(projectId)
                .forEach(subject -> {
                    String subjectId = subject.getId();
                    String table = "score_subject_" + subjectId;
                    Row projectRow = projectDao.queryFirst(QUERY_PROVINCE_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table));
                    if (projectRow != null) {
                        insertMap.add(createMap(Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.SUBJECT, subjectId, projectRow.getDouble("average", 0)));
                    }

                    String schoolTmp = QUERY_SCHOOL_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table);
                    String classTmp = QUERY_CLASS_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table);
                    schoolService.listSchool(projectId)
                            .forEach(school -> {
                                String schoolId = school.getId();
                                Row row = projectDao.queryFirst(schoolTmp.replace("{{schoolId}}", schoolId));
                                if (row != null) {
                                    insertMap.add(createMap(Range.SCHOOL, schoolId, Target.SUBJECT, subjectId, row.getDouble("average", 0)));
                                }
                            });

                    classService.listClasses(projectId)
                            .forEach(clazz -> {
                                String classId = clazz.getId();
                                Row row = projectDao.queryFirst(classTmp.replace("{{classId}}", classId));
                                if (row != null) {
                                    insertMap.add(createMap(Range.CLASS, classId, Target.SUBJECT, subjectId, row.getDouble("average", 0)));
                                }
                            });

                });
        projectDao.insert(insertMap, "high_score");

    }

    private void aggregateQuestHighScore(String projectId, DAO projectDao, double rate) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        processData(projectId, projectDao, rate, insertMap);
        projectDao.insert(insertMap, "high_score");
    }

    private void processData(String projectId, DAO projectDao, double rate, List<Map<String, Object>> insertMap) {
        questService.queryQuests(projectId)
                .stream()
                .forEach(quest -> {
                    String questId = quest.getId();
                    String table = "score_" + questId;
                    Row projectRow = projectDao.queryFirst(QUERY_PROVINCE_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table));
                    if (projectRow != null) {
                        insertMap.add(createMap(Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.SUBJECT, questId, projectRow.getDouble("average", 0)));
                    }

                    String schoolTmp = QUERY_SCHOOL_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table);
                    String classTmp = QUERY_CLASS_DATA.replace("{{rate}}", String.valueOf(rate)).replace("{{table}}", table);
                    schoolService.listSchool(projectId)
                            .forEach(school -> {
                                String schoolId = school.getId();
                                Row row = projectDao.queryFirst(schoolTmp.replace("{{schoolId}}", schoolId));
                                if (row != null) {
                                    insertMap.add(createMap(Range.SCHOOL, schoolId, Target.QUEST, questId, row.getDouble("average", 0)));
                                }
                            });

                    classService.listClasses(projectId)
                            .forEach(clazz -> {
                                String classId = clazz.getId();
                                Row row = projectDao.queryFirst(classTmp.replace("{{classId}}", classId));
                                if (row != null) {
                                    insertMap.add(createMap(Range.CLASS, classId, Target.QUEST, questId, row.getDouble("average", 0)));
                                }
                            });

                });
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, String targetType, String targetId, double score) {
        Map<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("target_type", targetType);
        map.put("target_id", targetId);
        map.put("score", score);
        return map;
    }

}
