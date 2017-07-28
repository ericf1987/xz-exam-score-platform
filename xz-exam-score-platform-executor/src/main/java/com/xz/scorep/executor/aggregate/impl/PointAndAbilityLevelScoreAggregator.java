package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.ajiaedu.common.lang.DoubleCounterMap;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.PointLevel;
import com.xz.scorep.executor.bean.SubjectLevel;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 知识点和能力层级得分统计
 *
 * @author by fengye on 2017/6/27.
 */
@AggregateTypes(AggregateType.Advanced)
@AggregateOrder(84)
@Component
public class PointAndAbilityLevelScoreAggregator extends Aggregator {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    SubjectService subjectService;

    public static final String QUERY_SCORE_BY_QUEST = "select student_id, '{{quest_id}}' quest_id, score from score_{{quest_id}} ";

    public static final Logger LOG = LoggerFactory.getLogger(PointAndAbilityLevelScoreAggregator.class);

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 学生知识点、能力层级、双向细目 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table score_point");
        projectDao.execute("truncate table score_point_level");
        projectDao.execute("truncate table score_subject_level");

        List<String> subjectIds = subjectService.listSubjects(projectId).stream()
                .map(ExamSubject::getId).collect(Collectors.toList());

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(10, 10, 10);

        for (String subjectId : subjectIds) {
            executor.submit(() -> doAggregate(projectId, projectDao, subjectId));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 学生知识点、能力层级、双向细目 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    public void doAggregate(String projectId, DAO projectDao, String subjectId) {
        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
        List<Row> rows = calculateQuestScoreBySubjectId(projectDao, examQuests);

        aggregateTotalScore(projectDao, subjectId, rows, examQuests);
    }

    //计算出所有学生当前科目的题目得分
    private List<Row> calculateQuestScoreBySubjectId(DAO projectDao, List<ExamQuest> examQuests) {
        List<String> result = new ArrayList<>();
        for (ExamQuest examQuest : examQuests) {
            String r = QUERY_SCORE_BY_QUEST.replace("{{quest_id}}", examQuest.getId());
            result.add(r);
        }

        String sql = StringUtil.joinPaths(" UNION ALL ", result);
        return projectDao.query(sql);
    }

    //统计总分
    private void aggregateTotalScore(DAO projectDao, String subjectId, List<Row> rows, List<ExamQuest> examQuests) {

        Map<String, DoubleCounterMap<String>> point_result_map = new HashMap<>();
        Map<String, DoubleCounterMap<PointLevel>> point_level_result_map = new HashMap<>();
        Map<String, DoubleCounterMap<SubjectLevel>> subject_level_result_map = new HashMap<>();
        for (Row row : rows) {
            String studentId = row.getString("student_id");
            String quest_id = row.getString("quest_id");
            double score = row.getDouble("score", 0);

            //查找到该题目对应的知识点
            Optional<String> first = examQuests.stream().filter(quest -> quest_id.equals(quest.getId())).map(ExamQuest::getPoints).findFirst();
            if (!first.isPresent()) {
                continue;
            }

            //小题对应的知识点

            String points = first.get();

            if(StringUtils.isBlank(points)){
                continue;
            }

            Map<String, Object> pointsMap = (Map) JSONUtils.parse(points);

            //在mongo统计库，如果小题没有知识点，会用Null标记知识点，所以这里必须处理

            DoubleCounterMap<String> point_counter_map = new DoubleCounterMap<>();
            DoubleCounterMap<PointLevel> point_Level_counter_map = new DoubleCounterMap<>();
            DoubleCounterMap<SubjectLevel> subject_level_counter_map = new DoubleCounterMap<>();

            //知识点得分结果计算
            if (point_result_map.containsKey(studentId)) {
                point_counter_map.putAll(point_result_map.get(studentId));
                for (String pointId : pointsMap.keySet()) {
                    point_counter_map.incre(pointId, score);
                }
            } else {
                for (String pointId : pointsMap.keySet()) {
                    point_counter_map.incre(pointId, score);
                }
            }

            //知识点能力层级结果计算
            if (point_level_result_map.containsKey(studentId)) {
                point_Level_counter_map.putAll(point_level_result_map.get(studentId));
                for (String pointId : pointsMap.keySet()) {
                    List<String> abilityLevels = (List<String>) pointsMap.get(pointId);
                    abilityLevels.forEach(level -> point_Level_counter_map.incre(new PointLevel(pointId, level), score));
                }
            } else {
                for (String pointId : pointsMap.keySet()) {
                    List<String> abilityLevels = (List<String>) pointsMap.get(pointId);
                    abilityLevels.forEach(level -> point_Level_counter_map.incre(new PointLevel(pointId, level), score));
                }
            }

            //科目能力层级结果计算
            if (subject_level_result_map.containsKey(studentId)) {
                subject_level_counter_map.putAll(subject_level_result_map.get(studentId));
                for (String pointId : pointsMap.keySet()) {
                    List<String> abilityLevels = (List<String>) pointsMap.get(pointId);
                    abilityLevels.forEach(level -> subject_level_counter_map.incre(new SubjectLevel(subjectId, level), score));
                }
            } else {
                for (String pointId : pointsMap.keySet()) {
                    List<String> abilityLevels = (List<String>) pointsMap.get(pointId);
                    abilityLevels.forEach(level -> subject_level_counter_map.incre(new SubjectLevel(subjectId, level), score));
                }
            }

            point_result_map.put(studentId, point_counter_map);
            point_level_result_map.put(studentId, point_Level_counter_map);
            subject_level_result_map.put(studentId, subject_level_counter_map);
        }

        savePointScore(projectDao, point_result_map);
        savePointLevelScore(projectDao, point_level_result_map);
        saveSubjectLevelScore(projectDao, subject_level_result_map);
    }

    private void savePointScore(DAO projectDao, Map<String, DoubleCounterMap<String>> pointResultMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String studentId : pointResultMap.keySet()) {
            DoubleCounterMap<String> counterMap = pointResultMap.get(studentId);
            for (String pointId : counterMap.keySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("student_id", studentId);
                m.put("point_id", pointId);
                m.put("total_score", counterMap.get(pointId));
                result.add(m);
            }
        }
        projectDao.insert(result, "score_point");
    }

    private void savePointLevelScore(DAO projectDao, Map<String, DoubleCounterMap<PointLevel>> pointLevelResultMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String studentId : pointLevelResultMap.keySet()) {
            DoubleCounterMap<PointLevel> counterMap = pointLevelResultMap.get(studentId);
            for (PointLevel pointLevel : counterMap.keySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("student_id", studentId);
                m.put("point", pointLevel.getPoint());
                m.put("level", pointLevel.getLevel());
                m.put("total_score", counterMap.get(pointLevel));
                result.add(m);
            }
        }
        projectDao.insert(result, "score_point_level");
    }

    private void saveSubjectLevelScore(DAO projectDao, Map<String, DoubleCounterMap<SubjectLevel>> subjectLevelResultMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String studentId : subjectLevelResultMap.keySet()) {
            DoubleCounterMap<SubjectLevel> counterMap = subjectLevelResultMap.get(studentId);
            for (SubjectLevel subjectLevel : counterMap.keySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("student_id", studentId);
                m.put("subject", subjectLevel.getSubject());
                m.put("level", subjectLevel.getLevel());
                m.put("total_score", counterMap.get(subjectLevel));
                result.add(m);
            }
        }
        projectDao.insert(result, "score_subject_level");
    }

}
