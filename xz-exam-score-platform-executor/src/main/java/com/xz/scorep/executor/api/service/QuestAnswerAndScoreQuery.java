package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.utils.SqlUtils.GroupType;

/**
 * 客观题，主观题详情
 * @author by fengye on 2017/7/6.
 */
@Component
public class QuestAnswerAndScoreQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    QuestService questService;

    @Autowired
    TopScoreRateQuery topScoreRateQuery;

    public Map<String, Object> queryQuestAnswerAndScore(String projectId, String subjectId, String rangeName, String rangeId) {
        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
        Map<String, Object> map = new HashMap<>();

        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());

        List<ExamQuest> objective = examQuests.stream().filter(q -> q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());
        List<ExamQuest> subjective = examQuests.stream().filter(q -> !q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());

        map.put("objectiveList", queryObjectiveResult(projectId, subjectId, rangeName, rangeId, objective));
        map.put("subjectiveList", querySubjectiveResult(projectId, subjectId, rangeName, rangeId, subjective));

        return map;
    }

    public List<Row> queryObjectiveResult(String projectId, String subjectId, String rangeName, String rangeId, List<ExamQuest> objective) {
        List<Row> scoreRate = topScoreRateQuery.getScoreRate(projectId, subjectId, rangeName, rangeId, objective, true, GroupType.AVG, GroupType.MAX);

        scoreRate.forEach(s -> {
            String questId = s.getString("questId");
            Optional<String> first = objective.stream().filter(o -> questId.equals(o.getId())).map(o -> o.getAnswer()).findFirst();
            s.put("answer", first.isPresent() ? first.get() : "");
        });

        return scoreRate;
    }

    public List<Row> querySubjectiveResult(String projectId, String subjectId, String rangeName, String rangeId, List<ExamQuest> subjective) {
        List<Row> scoreRate = topScoreRateQuery.getScoreRate(projectId, subjectId, rangeName, rangeId, subjective, true, GroupType.AVG, GroupType.MAX);
        return scoreRate;
    }

}
