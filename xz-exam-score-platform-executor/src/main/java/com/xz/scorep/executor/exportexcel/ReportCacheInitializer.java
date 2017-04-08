package com.xz.scorep.executor.exportexcel;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReportCacheInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ReportCacheInitializer.class);

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    public void initReportCache(String projectId) {
        initReportRankCache(projectId);

        LOG.info("开始缓存项目题目信息......");
        initReportQuestCache(projectId);
        LOG.info("缓存项目题目信息成功......");
    }


    private void initReportRankCache(String projectId) {
        initProvinceRankCache(projectId);
        initSchoolRankCache(projectId);
        initClassRankCache(projectId);
    }

    private List<Row> initProvinceRankCache(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_province";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_province")));
    }


    private List<Row> initSchoolRankCache(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_school";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_school")));
    }


    private List<Row> initClassRankCache(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_class";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_class")));
    }


    private Map<String, List<Row>> initReportQuestCache(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<ExamQuest> quests = questService.queryQuests(projectId);

        Map<String, List<Row>> result = new HashMap<>();
        quests.stream()
                .forEach(quest -> {
                    String questId = quest.getId();
                    String cacheKey = "quest_" + questId;
                    String sql = "select * from `score_" + questId + "`";
                    ArrayList<Row> rows = cache.get(cacheKey, () -> new ArrayList<>(projectDao.query(sql)));
                    result.put(cacheKey, rows);
                });
        return result;
    }


    public List<Row> queryProvinceRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initProvinceRankCache(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "province_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Row> querySchoolRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initSchoolRankCache(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "school_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Row> queryClassRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initClassRankCache(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "class_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }


    public List<Row> queryQuestCache(String projectId, List<String> student, String questId) {
        Map<String, List<Row>> result = initReportQuestCache(projectId);
        String cacheKey = "quest_" + questId;
        return result.get(cacheKey)
                .stream()
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "score_" + questId;
                    row.put(key, row.get("score"));
                    return row;
                })
                .collect(Collectors.toList());

    }

}
