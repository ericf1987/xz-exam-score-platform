package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分数分段
 */
@AggragateOrder(3)
@AggregateTypes(AggregateType.Basic)
@Component
public class ScoreSegmentsAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(ScoreSegmentsAggregator.class);

    public static final String SEGMENT_SELECTION = "" +
            "    @minscore := greatest(0, @step * FLOOR(score / @step)) as minscore,\n" +
            "    @maxscore := @minscore + @step - 0.5 as maxscore\n";

    private static final String PROVINCE_PROJECT_SEGMENT = "select " +
            "    a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            SEGMENT_SELECTION +
            "  from\n" +
            "    score_project, \n" +
            "    (select @step := {{step}}) x\n" +
            "  order by score" +
            ") a group by minscore,maxscore";

    private static final String SCHOOL_PROJECT_SEGMENT = "select school_id, a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            "    school.id as school_id,\n" +
            SEGMENT_SELECTION +
            "  from score_project, student, class, school,\n" +
            "    (select @step := {{step}}) x\n" +
            "  where\n" +
            "    score_project.student_id=student.id and\n" +
            "    student.class_id=class.id and\n" +
            "    class.school_id=school.id\n" +
            "  order by score\n" +
            ") a group by school_id,minscore,maxscore";

    private static final String CLASS_PROJECT_SEGMENT = "select class_id, a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            "    class.id as class_id,\n" +
            SEGMENT_SELECTION +
            "  from score_project, student, class,\n" +
            "    (select @step := {{step}}) x\n" +
            "  where\n" +
            "    score_project.student_id=student.id and\n" +
            "    student.class_id=class.id\n" +
            "  order by score\n" +
            ") a group by class_id,minscore,maxscore";

    private static final String PROVINCE_SUBJECT_SEGMENT = "select a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            "    student_id, score,\n" +
            SEGMENT_SELECTION +
            "  from score_subject_{{subject}},\n" +
            "    (select @step := {{step}}) x\n" +
            "  order by score\n" +
            ") a group by minscore,maxscore";

    private static final String SCHOOL_SUBJECT_SEGMENT = "select school_id, a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            "    school.id as school_id,\n" +
            SEGMENT_SELECTION +
            "  from score_subject_{{subject}}, student, class, school,\n" +
            "    (select @step := {{step}}) x\n" +
            "  where\n" +
            "    score_subject_{{subject}}.student_id=student.id and\n" +
            "    student.class_id=class.id and\n" +
            "    class.school_id=school.id\n" +
            "  order by score\n" +
            ") a group by school_id,minscore,maxscore";

    private static final String CLASS_SUBJECT_SEGMENT = "select class_id, a.minscore, a.maxscore, count(1) as `count` from (\n" +
            "  select\n" +
            "    class.id as class_id,\n" +
            SEGMENT_SELECTION +
            "  from score_subject_{{subject}}, student, class,\n" +
            "    (select @step := {{step}}) x\n" +
            "  where\n" +
            "    score_subject_{{subject}}.student_id=student.id and\n" +
            "    student.class_id=class.id\n" +
            "  order by score\n" +
            ") a group by class_id,minscore,maxscore";

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table segments");
        LOG.info("成绩分段统计结果已清空。");

        aggrTotalScoreSegments(projectId, projectDao);
        aggrSubjectScoreSegments(projectId, projectDao);
    }

    private void aggrSubjectScoreSegments(String projectId, DAO projectDao) throws InterruptedException {
        ThreadPools.createAndRunThreadPool(10, 100, pool ->
                subjectService.listSubjects(projectId).forEach(subject -> {
                    String subjectId = subject.getId();
                    pool.submit(() -> aggrSubjectScoreSegments0(projectId, projectDao, subjectId));
                }));
    }

    private void aggrSubjectScoreSegments0(String projectId, DAO projectDao, String subjectId) {
        // 总体科目成绩分段
        String provinceSql = PROVINCE_SUBJECT_SEGMENT.replace("{{step}}", "10").replace("{{subject}}", subjectId);
        List<Map<String, Object>> provinceSegmentRows = new ArrayList<>();

        projectDao.query(provinceSql).forEach(row -> {
            String province = "430000";
            provinceSegmentRows.add(createCountMap(
                    row, Range.province(province), Target.subject(subjectId)));
        });

        projectDao.insert(provinceSegmentRows, "segments");
        LOG.info("项目 {} 的科目 {} 总体科目成绩分段统计完成", projectId, subjectId);

        //////////////////////////////////////////////////////////////

        // 学校科目成绩分段
        String schoolSql = SCHOOL_SUBJECT_SEGMENT.replace("{{step}}", "10").replace("{{subject}}", subjectId);
        List<Map<String, Object>> schoolSegmentRows = new ArrayList<>();

        projectDao.query(schoolSql).forEach(row -> {
            String schoolId = row.getString("school_id");
            schoolSegmentRows.add(createCountMap(row, Range.school(schoolId), Target.subject(subjectId)));
        });

        projectDao.insert(schoolSegmentRows, "segments");
        LOG.info("项目 {} 的科目 {} 学校科目成绩分段统计完成", projectId, subjectId);

        //////////////////////////////////////////////////////////////

        // 班级科目成绩分段
        String classSql = CLASS_SUBJECT_SEGMENT.replace("{{step}}", "10").replace("{{subject}}", subjectId);
        List<Map<String, Object>> classSegmentRows = new ArrayList<>();

        projectDao.query(classSql).forEach(row -> {
            String classId = row.getString("class_id");
            classSegmentRows.add(createCountMap(row, Range.clazz(classId), Target.subject(subjectId)));
        });

        projectDao.insert(classSegmentRows, "segments");
        LOG.info("项目 {} 的科目 {} 班级科目成绩分段统计完成", projectId, subjectId);
    }

    private void aggrTotalScoreSegments(String projectId, DAO projectDao) {

        // 总体总分成绩分段
        projectDao.query(PROVINCE_PROJECT_SEGMENT.replace("{{step}}", "50")).forEach(row -> {
            Map<String, Object> map = createCountMap(row,
                    Range.PROVINCE_RANGE, Target.project(projectId));
            projectDao.insert(map, "segments");
        });
        LOG.info("项目 {} 的总体总分成绩分段统计完成", projectId);

        //////////////////////////////////////////////////////////////

        // 学校总分成绩分段
        List<Map<String, Object>> schoolSegmentRows = new ArrayList<>();

        projectDao.query(SCHOOL_PROJECT_SEGMENT.replace("{{step}}", "50")).forEach(row -> {
            String schoolId = row.getString("school_id");
            schoolSegmentRows.add(createCountMap(row, Range.school(schoolId), Target.project(projectId)));
        });

        projectDao.insert(schoolSegmentRows, "segments");
        LOG.info("项目 {} 的学校总分成绩分段统计完成", projectId);

        //////////////////////////////////////////////////////////////

        // 班级总分成绩分段
        List<Map<String, Object>> classSegmentRows = new ArrayList<>();

        projectDao.query(CLASS_PROJECT_SEGMENT.replace("{{step}}", "50")).forEach(row -> {
            String classId = row.getString("class_id");
            classSegmentRows.add(createCountMap(row, Range.clazz(classId), Target.project(projectId)));
        });

        projectDao.insert(classSegmentRows, "segments");
        LOG.info("项目 {} 的班级总分成绩分段统计完成", projectId);
    }

    private Map<String, Object> createCountMap(
            Row row,
            Range range, Target target) {

        Map<String, Object> map = new HashMap<>();
        map.put("range_type", range.getType());
        map.put("range_id", range.getId());
        map.put("target_type", target.getType());
        map.put("target_id", target.getId());

        map.put("score_min", row.getDoubleObject("minscore"));
        map.put("score_max", row.getDoubleObject("maxscore"));
        map.put("student_count", row.getIntegerObject("count"));

        return map;
    }
}
