package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主观题客观题得分详情查询
 *
 * @author luckylo
 */
@Service
public class SubjectiveObjectiveService {

    @Autowired
    private SubjectiveObjectiveQuery query;

    @Autowired
    private QuestService questService;

    //主观题得分详情
    public List<Map<String, Object>> querySubjectiveScoreDetail(String database, String subjectId, String classId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuestsFromBak(database, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> subjectiveQuests = quests.stream()
                .filter(q -> !q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());

        subjectiveQuests.forEach(quest -> fillStudentScore(database, subjectId, classId, studentId, quest, result));
        return result;
    }

    private void fillStudentScore(String database, String subjectId, String classId, String studentId, ExamQuest quest, List<Map<String, Object>> result) {
        Map<String, Object> map = new HashMap<>();
        String questId = quest.getId();
        String questNo = quest.getQuestNo();
        map.put("questNo", questNo);

        Row studentRow = query.queryStudentRow(database, subjectId, studentId, questId);

        if (quest.isObjective()) {//客观题
            String studentAnswer = studentRow.getString("objective_answer");
            map.put("studentAnswer", studentAnswer);
            Optional<Row> detailRow = query.queryObjectiveDetail(database, subjectId, questId, classId);
            if (!detailRow.isPresent()) {
                return;
            }
            String answer = detailRow.get().getString("answer");
            double score = DoubleUtils.round(detailRow.get().getDouble("score_rate", 0) * 100, true);


            map.put("answer", answer);
            map.put("scoreRate", score + "%");
        } else {//主观题
            double score = studentRow.getDouble("score", 0);
            map.put("score", score);
            Optional<Row> detailRow = query.querySubjectiveDetail(database, subjectId, questId, classId);
            if (!detailRow.isPresent()) {
                return;
            }

            double averageScore = detailRow.get().getDouble("average_score", 0);
            double maxScore = detailRow.get().getDouble("max_score", 0);

            map.put("averageScore", averageScore);
            map.put("maxScore", maxScore);
        }

        result.add(map);
    }


    //客观题得分详情
    public List<Map<String, Object>> queryObjectiveScoreDetail(String database, String subjectId, String classId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuestsFromBak(database, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> objectiveQuests = quests.stream()
                .filter(ExamQuest::isObjective).sorted(questNoComparator).collect(Collectors.toList());

        objectiveQuests.forEach(quest -> fillStudentScore(database, subjectId, classId, studentId, quest, result));
        return result;
    }


    //主观题 : 与班级单题得分差距较大的TOP5(差值的绝对值最大)
    public List<Map<String, Object>> querySubjectiveTop5(String database, String subjectId, String studentId, String classId) {
        List<Map<String, Object>> list = new ArrayList<>();

        questService.queryQuests(database)
                .stream()
                .filter(quest -> !quest.isObjective() && subjectId.equals(quest.getExamSubject()))
                .forEach(quest -> queryStudentSubjectiveEachRow(database, subjectId, studentId, classId, list, quest));

        if (list.isEmpty()) {
            return null;
        }

        Collections.sort(list, (Map<String, Object> m1, Map<String, Object> m2) -> {
            Double s1 = MapUtils.getDouble(m1, "dValue");
            Double s2 = MapUtils.getDouble(m2, "dValue");
            return s2.compareTo(s1);
        });

        return list.stream().limit(5).collect(Collectors.toList());
    }

    ///主观题 : 与班级单题得分差距较大的TOP5(差值的绝对值最大)  每一行
    private void queryStudentSubjectiveEachRow(String database, String subjectId, String studentId, String classId, List<Map<String, Object>> list, ExamQuest quest) {
        String questId = quest.getId();
        Row studentRow = query.queryStudentSubjectiveQuest(database, subjectId, questId, studentId);
        if (studentRow != null) {
            Map<String, Object> map = new HashMap<>();
            Row averageRow = query.queryClassAverageScore(database,subjectId, questId, classId);

            double score = studentRow.getDouble("score", 0);
            double average_score = averageRow.getDouble("average_score", 0);

            map.put("quest_no", quest.getQuestNo());
            map.put("full_score", quest.getFullScore());
            map.put("score", score);
            map.put("average_score", average_score);
            map.put("dValue", DoubleUtils.round(Math.abs(average_score - score)));

            list.add(map);
        }
    }

    //客观题 : 与班级单题得分人数最多,且自己没得满分的题目TOP5 (人数为得满分人数)
    public List<Map<String, Object>> queryObjectiveTop5(String database, String subjectId, String classId, String studentId) {
        List<Map<String, Object>> studentFailList = new ArrayList<>();
        questService.queryQuests(database)
                .stream()
                .filter(quest -> subjectId.equals(quest.getExamSubject()) && quest.isObjective())
                .forEach(quest -> queryObjectiveTop5EachRow(database, subjectId, classId, studentId, studentFailList, quest));

        if (studentFailList.isEmpty()) {
            return null;
        }

        //少于5直接返回,超过5就取前5
        List<Map<String, Object>> collect = studentFailList.stream()
                .sorted((map1, map2) -> (int) map2.get("correct_count") - (int) map1.get("correct_count"))
                .collect(Collectors.toList());

        return collect.stream().limit(5).collect(Collectors.toList());
    }

    //客观题与班级答对人数差距较大的每一行
    private void queryObjectiveTop5EachRow(String database, String subjectId, String classId, String studentId, List<Map<String, Object>> studentFailList, ExamQuest quest) {
        String questId = quest.getId();
        Row row = query.queryStudentFalseObjectiveQuest(database, subjectId, questId, studentId);
        if (null != row) {
            //查该题在班级中答对的人数
            Map<String, Object> map = new HashMap<>();
            int correctCount = query.queryStudentCorrectCount(database, subjectId, questId, classId);

            map.put("correct_count", correctCount);
            map.put("quest_no", quest.getQuestNo());
            map.put("full_score", quest.getFullScore());
            map.put("score", row.getDouble("score", 0));

            studentFailList.add(map);

        }
    }


    //主观题(总分,我的得分,得分排名,班级平均分,班级最高分)
    public Map<String, Object> subjectiveDetail(String database, String subjectId, String classId, String studentId) {
        Map<String, Double> fullScore = query.querySubjectiveObjectiveFullScore(database, subjectId);
        Map<String, Object> subjective = query.querySubjectiveScoreRank(database, subjectId, classId, studentId);
        subjective.put("fullScore", fullScore.get("subjective"));
        return subjective;
    }


    //客观题(总分,我的得分,得分排名,班级平均分,班级最高分)
    public Map<String, Object> objectiveDetail(String database, String subjectId, String classId, String studentId) {
        Map<String, Double> fullScore = query.querySubjectiveObjectiveFullScore(database, subjectId);
        Map<String, Object> subjective = query.queryObjectiveScoreRank(database, subjectId, classId, studentId);
        subjective.put("fullScore", fullScore.get("objective"));
        return subjective;
    }
}
