package com.xz.scorep.executor.project;

import com.hyd.simplecache.SimpleCache;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestService {

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private CacheFactory cacheFactory;

    public void clearQuests(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table quest");
    }

    public void saveQuest(String projectId, ExamQuest examQuest) {
        daoFactory.getProjectDao(projectId).insert(examQuest, "quest");
    }

    public void saveQuest(String projectId, List<ExamQuest> examQuests) {
        daoFactory.getProjectDao(projectId).insert(examQuests, "quest");
    }

    public ExamQuest findQuest(String projectId, String questId) {
        return daoFactory.getProjectDao(projectId).queryFirst(
                ExamQuest.class, "select * from quest where id=?", questId);
    }

    public List<ExamQuest> queryQuests(String projectId) {
        SimpleCache cache = cacheFactory.getProjectCache(projectId);
        String cacheKey = "quests:";

        return cache.get(cacheKey, () ->
                fixQuestList(daoFactory.getProjectDao(projectId).query(ExamQuest.class, "select * from quest")));
    }

    public List<ExamQuest> queryQuests(String projectId, String subjectId) {
        return queryQuests(projectId).stream()
                .filter(q -> q.getExamSubject().equals(subjectId) || q.getQuestSubject().equals(subjectId))
                .collect(Collectors.toList());
    }

    public List<ExamQuest> queryQuests(String projectId, boolean objective) {
        return queryQuests(projectId).stream()
                .filter(q -> q.isObjective() == objective)
                .collect(Collectors.toList());
    }

    public List<ExamQuest> queryQuests(String projectId, String subjectId, boolean objective) {
        return queryQuests(projectId).stream()
                .filter(q -> q.getExamSubject().equals(subjectId) && q.isObjective() == objective)
                .collect(Collectors.toList());
    }

    private ArrayList<ExamQuest> fixQuestList(List<ExamQuest> list) {
        list.sort(NaturalOrderComparator.getComparator(ExamQuest::getQuestNo));
        return new ArrayList<>(list);
    }
}
