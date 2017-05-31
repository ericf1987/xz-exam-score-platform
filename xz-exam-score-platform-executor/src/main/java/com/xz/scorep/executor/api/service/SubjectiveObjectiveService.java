package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.DoubleUtils;
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
    public List<Map<String, Object>> querySubjectiveScoreDetail(String projectId, String subjectId, String classId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuests(projectId, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> subjectiveQuests = quests.stream()
                .filter(q -> !q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());

        subjectiveQuests.forEach(quest -> fillStudentScore(projectId, classId, studentId, quest, result));
        return result;
    }

    private void fillStudentScore(String projectId, String classId, String studentId, ExamQuest quest, List<Map<String, Object>> result) {
        Map<String, Object> map = new HashMap<>();
        String questId = quest.getId();
        String questNo = quest.getQuestNo();
        map.put("questNo", questNo);

        Row studentRow = query.queryStudentRow(projectId, studentId, questId);

        if (quest.isObjective()) {//客观题
            String studentAnswer = studentRow.getString("objective_answer");
            map.put("studentAnswer", studentAnswer);
            Row detailRow = query.queryObjectiveDetail(projectId, questId, classId);

            String answer = detailRow.getString("answer");
            String scoreRate = DoubleUtils.toPercent(detailRow.getDouble("score_rate", 0));

            map.put("answer", answer);
            map.put("scoreRate", scoreRate);
        } else {//主观题
            double score = studentRow.getDouble("score", 0);
            map.put("score", score);
            Row detailRow = query.querySubjectiveDetail(projectId, questId, classId);

            double averageScore = detailRow.getDouble("average_score", 0);
            double maxScore = detailRow.getDouble("max_score", 0);

            map.put("averageScore", averageScore);
            map.put("maxScore", maxScore);
        }

        result.add(map);
    }

    //客观题得分详情
    public List<Map<String, Object>> queryObjectiveScoreDetail(String projectId, String subjectId, String classId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuests(projectId, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> objectiveQuests = quests.stream()
                .filter(ExamQuest::isObjective).sorted(questNoComparator).collect(Collectors.toList());

        objectiveQuests.forEach(quest -> fillStudentScore(projectId, classId, studentId, quest, result));
        return result;
    }


    //主观题 : 与班级单题得分差距较大的TOP5(差值的绝对值最大)
    public List<Map<String, Object>> querySubjectiveTop5(String projectId, String subjectId, String studentId, String classId) {

        List<Map<String, Object>> list = new ArrayList<>();

        questService.queryQuests(projectId)
                .stream()
                .filter(quest -> !quest.isObjective() && subjectId.equals(quest.getExamSubject()))
                .forEach(quest -> queryStudentSubjectiveEachRow(projectId, studentId, classId, list, quest));

        if (list.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> collect = list.stream()
                .sorted((map1, map2) -> sorted(map1, map2))
                .limit(5)
                .collect(Collectors.toList());

        return collect;
    }

    ///主观题 : 与班级单题得分差距较大的TOP5(差值的绝对值最大)  每一行
    private void queryStudentSubjectiveEachRow(String projectId, String studentId, String classId, List<Map<String, Object>> list, ExamQuest quest) {
        String questId = quest.getId();
        Row studentRow = query.queryStudentSubjectiveQuest(projectId, questId, studentId);
        if (studentRow != null) {
            Map<String, Object> map = new HashMap<>();
            Row averageRow = query.queryClassAverageScore(projectId, questId, classId);

            map.put("quest_no", quest.getQuestNo());
            map.put("full_score", quest.getFullScore());
            map.put("score", studentRow.getDouble("score", 0));
            map.put("average_score", averageRow.getDouble("average_score", 0));

            list.add(map);
        }
    }

    private int sorted(Map<String, Object> map1, Map<String, Object> map2) {
        double score1 = (double) map1.get("score");
        double averageScore1 = (double) map1.get("average_score");
        double sub1 = Math.abs((averageScore1 - score1));

        double score2 = (double) map2.get("score");
        double averageScore2 = (double) map2.get("average_score");
        double sub2 = Math.abs((averageScore2 - score2));

        return (sub2 > sub1) ? 1 : 0;
    }


    //客观题 : 与班级单题得分人数最多,且自己没得满分的题目TOP5 (人数为得满分人数)
    public List<Map<String, Object>> queryObjectiveTop5(String projectId, String subjectId, String classId, String studentId) {
        List<Map<String, Object>> studentFailList = new ArrayList<>();

        questService.queryQuests(projectId)
                .stream()
                .filter(quest -> subjectId.equals(quest.getExamSubject()) && quest.isObjective())
                .forEach(quest -> queryObjectiveTop5EachRow(projectId, classId, studentId, studentFailList, quest));

        if (studentFailList.isEmpty()) {
            return null;
        }

        //少于5直接返回,超过5就取前5
        List<Map<String, Object>> collect = studentFailList.stream()
                .sorted((map1, map2) -> (int) map2.get("correct_count") - (int) map1.get("correct_count"))
                .limit(5)
                .collect(Collectors.toList());

        return collect;
    }

    //客观题与班级答对人数差距较大的每一行
    private void queryObjectiveTop5EachRow(String projectId, String classId, String studentId, List<Map<String, Object>> studentFailList, ExamQuest quest) {
        String questId = quest.getId();
        Row row = query.queryStudentFalseObjectiveQuest(projectId, questId, studentId);
        if (null != row) {
            //查该题在班级中答对的人数
            Map<String, Object> map = new HashMap<>();
            int correctCount = query.queryStudentCorrectCount(projectId, questId, classId);

            map.put("correct_count", correctCount);
            map.put("quest_no", quest.getQuestNo());
            map.put("full_score", quest.getFullScore());
            map.put("score", row.getDouble("score", 0));

            studentFailList.add(map);

        }
    }


    //主观题(总分,我的得分,得分排名,班级平均分,班级最高分)
    public Map<String, Object> subjectiveDetail(String projectId, String subjectId, String classId, String studentId) {
        Map<String, Double> fullScore = query.querySubjectiveObjectiveFullScore(projectId, subjectId);
        Map<String, Object> subjective = query.querySubjectiveScoreRank(projectId, subjectId, classId, studentId);
        subjective.put("fullScore", fullScore.get("subjective"));
        return subjective;
    }


    //客观题(总分,我的得分,得分排名,班级平均分,班级最高分)
    public Map<String, Object> objectiveDetail(String projectId, String subjectId, String classId, String studentId) {
        Map<String, Double> fullScore = query.querySubjectiveObjectiveFullScore(projectId, subjectId);
        Map<String, Object> subjective = query.queryObjectiveScoreRank(projectId, subjectId, classId, studentId);
        subjective.put("fullScore", fullScore.get("objective"));
        return subjective;
    }
}
