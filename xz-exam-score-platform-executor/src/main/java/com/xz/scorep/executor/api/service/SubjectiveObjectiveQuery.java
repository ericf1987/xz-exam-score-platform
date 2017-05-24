package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 主观题,客观题得分详情
 *
 * @author luckylo
 */
@Component
public class SubjectiveObjectiveQuery {

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

    //查询学生答案
    public Row queryStudentRow(String projectId, String studentId, String questId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "quest:" + questId;
        String table = "score_" + questId;
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from `" + table + "`"));
        });

        return rows.stream()
                .filter(row -> studentId.equals(row.getString("student_id")))
                .findFirst()
                .get();
    }

    //查询客观题得分详情,每一道题的正确答案和班级得分率....
    public Row queryObjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "objective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from objective_score_rate"));
        });
        return rows.stream()
                .filter(row -> questId.equals(row.getString("quest_id")) && classId.equals(row.getString("range_id")))
                .findFirst().get();
    }


    //查询主观题得分详情...每一道题的最高分做低分
    public Row querySubjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "subjective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from quest_average_max_score"));
        });
        return rows.stream()
                .filter(row -> "Class".equals(row.getString("range_type")))
                .filter(row -> classId.equals(row.getString("range_id")))
                .filter(row -> questId.equals(row.getString("quest_id")))
                .findFirst().get();

    }

}
