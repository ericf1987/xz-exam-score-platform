package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.ajiaedu.common.report.Keys.Range;
import com.xz.ajiaedu.common.report.Keys.Target;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.reportconfig.ScoreLevelsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 成绩四率分布
 */
@AggregateTypes({AggregateType.Basic, AggregateType.Quick})
@AggragateOrder(4)
@Component
public class ScoreLevelRateAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(ScoreLevelRateAggregator.class);

    public static final String CLASS_PROJECT_SCORE_LEVEL = "select " +
            "  a.class_id as range_id, a.`level`, count(1) as `count` from (\n" +
            "  select class.id as class_id, (case \n" +
            "    when score<{{pass}} then 'FAIL'\n" +
            "    when score<{{good}} then 'PASS'\n" +
            "    when score<{{xlnt}} then 'GOOD'\n" +
            "    else 'XLNT' end) as `level`\n" +
            "  from {{table}} s, student, class\n" +
            "  where \n" +
            "    s.student_id=student.id and \n" +
            "    student.class_id=class.id \n" +
            ") a group by a.`level`, a.class_id";

    public static final String SCHOOL_PROJECT_SCORE_LEVEL = "select " +
            "  a.school_id as range_id, a.`level`, count(1) as `count` from (\n" +
            "  select school.id as school_id, (case \n" +
            "    when score<{{pass}} then 'FAIL'\n" +
            "    when score<{{good}} then 'PASS'\n" +
            "    when score<{{xlnt}} then 'GOOD'\n" +
            "    else 'XLNT' end) as `level`\n" +
            "  from {{table}} s, student, class, school\n" +
            "  where \n" +
            "    s.student_id=student.id and \n" +
            "    student.class_id=class.id and \n" +
            "    class.school_id=school.id\n" +
            ") a group by a.`level`, a.school_id";

    public static final String PROVINCE_PROJECT_SCORE_LEVEL = "select " +
            "  a.`level`, count(1) as `count` from (\n" +
            "  select (case \n" +
            "    when score<{{pass}} then 'FAIL'\n" +
            "    when score<{{good}} then 'PASS'\n" +
            "    when score<{{xlnt}} then 'GOOD'\n" +
            "    else 'XLNT' end) as `level`\n" +
            "  from {{table}} s\n" +
            ") a group by a.`level`";

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        String scoreLevelConfig = reportConfig.getScoreLevelConfig();
        JSONObject scoreLevels = JSON.parseObject(reportConfig.getScoreLevels());

        LOG.info("开始统计四率...");
        daoFactory.getProjectDao(projectId).execute("truncate table scorelevelmap");
        LOG.info("scorelevelmap 内容已清除");

        List<ExamSubject> subjects = getExamSubjects(aggregateParameter, projectId);

        LOG.info("统计整体成绩四率...");
        aggregateProjectScoreLevels(projectId, scoreLevels, scoreLevelConfig);

        List<String> subjectIdList = subjects.stream().map(ExamSubject::getId).collect(Collectors.toList());
        LOG.info("统计科目成绩四率...,科目列表:{}", subjectIdList);
        aggregateSubjectScoreLevels(projectId, scoreLevels, subjects, scoreLevelConfig);
    }

    private List<ExamSubject> getExamSubjects(AggregateParameter aggregateParameter, String projectId) {
        List<ExamSubject> subjects;

        if (aggregateParameter.getSubjects().isEmpty()) {
            subjects = subjectService.listSubjects(projectId);

        } else {
            subjects = aggregateParameter.getSubjects()
                    .stream().map(subjectId -> subjectService.findSubject(projectId, subjectId))
                    .collect(Collectors.toList());
        }
        return subjects;
    }

    private Map<String, Object> createMap(
            Map<String, Integer> totalMap, Row row,
            Target targetType, String targetId) {

        String rangeId = StringUtil.or(row.getString("range_id"), "430000");
        int count = row.getInteger("count", 0);
        int total = totalMap.getOrDefault(rangeId, 0);

        Map<String, Object> map = new HashMap<>();
        map.put("target_type", targetType);
        map.put("target_id", targetId);
        map.put("student_count", count);
        map.put("score_level", row.getString("level"));
        map.put("student_rate", total == 0 ? 0 : ((double) count / total * 100));
        return map;
    }

    private void aggregateProjectScoreLevels(String projectId, JSONObject scoreLevels, String scoreLevelConfig) {
        ExamProject project = projectService.findProject(projectId);
        double fullScore = project.getFullScore();

        aggrProjectScoreLevel(projectId, "score_project", scoreLevels, scoreLevelConfig, fullScore, PROVINCE_PROJECT_SCORE_LEVEL,
                Target.Project, projectId,
                (row, map) -> {
                    map.put("range_type", Range.Province.name());
                    map.put("range_id", "430000");
                });

        aggrProjectScoreLevel(projectId, "score_project", scoreLevels, scoreLevelConfig, fullScore, SCHOOL_PROJECT_SCORE_LEVEL,
                Target.Project, projectId,
                (row, map) -> {
                    map.put("range_type", Range.School.name());
                    map.put("range_id", row.getString("range_id"));
                });

        aggrProjectScoreLevel(projectId, "score_project", scoreLevels, scoreLevelConfig, fullScore, CLASS_PROJECT_SCORE_LEVEL,
                Target.Project, projectId,
                (row, map) -> {
                    map.put("range_type", Range.Class.name());
                    map.put("range_id", row.getString("range_id"));
                });
    }

    private void aggregateSubjectScoreLevels(String projectId, JSONObject scoreLevels, List<ExamSubject> examSubjects, String scoreLevelConfig) {
        examSubjects.forEach(subject -> {
            double fullScore = subject.getFullScore();
            String tableName = "score_subject_" + subject.getId();

            aggrProjectScoreLevel(projectId, tableName, scoreLevels, scoreLevelConfig, fullScore, PROVINCE_PROJECT_SCORE_LEVEL,
                    Target.Subject, subject.getId(),
                    (row, map) -> {
                        map.put("range_type", Range.Province.name());
                        map.put("range_id", "430000");
                    });

            aggrProjectScoreLevel(projectId, tableName, scoreLevels, scoreLevelConfig, fullScore, SCHOOL_PROJECT_SCORE_LEVEL,
                    Target.Subject, subject.getId(),
                    (row, map) -> {
                        map.put("range_type", Range.School.name());
                        map.put("range_id", row.getString("range_id"));
                    });

            aggrProjectScoreLevel(projectId, tableName, scoreLevels, scoreLevelConfig, fullScore, CLASS_PROJECT_SCORE_LEVEL,
                    Target.Subject, subject.getId(),
                    (row, map) -> {
                        map.put("range_type", Range.Class.name());
                        map.put("range_id", row.getString("range_id"));
                    });
        });
    }

    private void aggrProjectScoreLevel(
            String projectId, String tableName, JSONObject scoreLevels, String scoreLevelConfig, double fullScore, String sqlTemplate,
            Target target, String targetId,
            BiConsumer<Row, Map<String, Object>> mapFixer) {

        String subjectId = target.name().equals("Project") ? "000" : targetId;

        String sql = sqlTemplate
                .replace("{{table}}", tableName)
                .replace("{{pass}}", String.valueOf(ScoreLevelsHelper.passScore(subjectId, scoreLevels, scoreLevelConfig, fullScore)))
                .replace("{{good}}", String.valueOf(ScoreLevelsHelper.goodScore(subjectId, scoreLevels, scoreLevelConfig, fullScore)))
                .replace("{{xlnt}}", String.valueOf(ScoreLevelsHelper.excellentScore(subjectId, scoreLevels, scoreLevelConfig, fullScore)));

        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> scoreMapRows = projectDao.query(sql);

        Map<String, Integer> totalMap = new HashMap<>();
        scoreMapRows.forEach(row -> {
            int count = row.getInteger("count", 0);
            String rangeId = StringUtil.or(row.getString("range_id"), "430000");

            if (!totalMap.containsKey(rangeId)) {
                totalMap.put(rangeId, count);
            } else {
                totalMap.put(rangeId, count + totalMap.get(rangeId));
            }
        });

        List<Map<String, Object>> insertMaps = new ArrayList<>();

        scoreMapRows.forEach(row -> {
            Map<String, Object> map = createMap(totalMap, row, target, targetId);
            if (mapFixer != null) {
                mapFixer.accept(row, map);
            }
            insertMaps.add(map);
        });

        projectDao.insert(insertMaps, "scorelevelmap");
    }
}
