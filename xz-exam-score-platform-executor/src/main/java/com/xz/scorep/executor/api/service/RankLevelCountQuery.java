package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 各等第人数统计
 *
 * @author by fengye on 2017/7/27.
 */
@Component
public class RankLevelCountQuery {
    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    public static final String QUERY_DATA = "SELECT rank_level, COUNT(1) cnt FROM rank_level_class rank_level, student stu\n" +
            "WHERE rank_level.student_id = stu.`id`\n" +
            "AND stu.class_id = {{class_id}}\n" +
            "AND rank_level.subject_id = {{subject_id}}\n" +
            "GROUP BY rank_level ORDER BY rank_level ASC";

    public List<Row> questRankLevelCount(String projectId, String classId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        String sql = QUERY_DATA.replace("{{class_id}}", classId).replace("{{subject_id}}", subjectId);

        String cacheKey = "rank_level_count:" + projectId + ":" + classId + ":" + subjectId;

        return cacheFactory.getProjectCache(projectId).get(cacheKey, () -> new ArrayList<>(projectDao.query(sql)));

    }
}
