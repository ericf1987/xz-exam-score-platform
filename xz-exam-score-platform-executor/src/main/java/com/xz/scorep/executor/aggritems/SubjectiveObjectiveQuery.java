package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主观题客观题得分详情查询
 *
 * @author luckylo
 */
@Component
public class SubjectiveObjectiveQuery {

    private static final String QUERY_SUBJECTIVE_SCORE_DETAIL = "";

    private static final String QUERY_OBJECTIVE_SCORE_DETAIL = "";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ReportCacheInitializer reportCache;

    @Autowired
    private QuestService questService;

    //主观题得分详情
    public List<Map<String, Object>> querySubjectiveScoreDetail(String projectId, String subjectId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuests(projectId, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> subjectiveQuests = quests.stream()
                .filter(q -> !q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());

        subjectiveQuests.forEach(quest -> fillStudentScore(projectId, studentId, quest));
        return result;
    }

    private void fillStudentScore(String projectId, String studentId, ExamQuest quest) {
        String questId = quest.getId();
        String questNo = quest.getQuestNo();

        List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, Arrays.asList(studentId), questId);
        if (rows.isEmpty()) {
            return;
        }
        Row row = rows.get(0);
        if (quest.isObjective()) {//客观题
            String answer = row.getString("objective_answer");
            // TODO: 2017-05-24 班级得分率
        } else {//主观题
            double score = row.getDouble("score", 0);
            // TODO: 2017-05-24 班级最高分
        }
    }

    //客观题得分详情
    public List<Map<String, Object>> queryObjectiveScoreDetail(String projectId, String subjectId, String studentId) {
        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuests(projectId, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<ExamQuest> objectiveQuests = quests.stream()
                .filter(ExamQuest::isObjective).sorted(questNoComparator).collect(Collectors.toList());

        objectiveQuests.forEach(quest -> fillStudentScore(projectId, studentId, quest));
        return result;
    }
}
