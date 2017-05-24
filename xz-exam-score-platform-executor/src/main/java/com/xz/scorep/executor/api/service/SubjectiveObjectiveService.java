package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
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
    private ReportCacheInitializer reportCache;

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
        if (studentRow.isEmpty()) {
            return;
        }

        if (quest.isObjective()) {//客观题
            String studentAnswer = studentRow.getString("objective_answer");
            map.put("studentAnswer", studentAnswer);
            Row detailRow = query.queryObjectiveDetail(projectId, questId, classId);

            if (detailRow == null) {
                return;
            }

            String answer = detailRow.getString("answer");
            String scoreRate = DoubleUtils.toPercent(detailRow.getDouble("score_rate", 0));

            map.put("answer", answer);
            map.put("scoreRate", scoreRate);
        } else {//主观题
            double score = studentRow.getDouble("score", 0);
            map.put("score", score);
            Row detailRow = query.querySubjectiveDetail(projectId, questId, classId);

            if (detailRow == null) {
                return;
            }

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
}
