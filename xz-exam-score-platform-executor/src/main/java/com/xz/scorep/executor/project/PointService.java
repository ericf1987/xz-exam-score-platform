package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.bean.Point;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author by fengye on 2017/6/26.
 */
@Service
public class PointService {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    public void savePoint(DAO projectDao, String pointId, String point_name, String parent_point_id, String subject) {
        Point point = new Point(pointId, point_name, parent_point_id, subject);
        projectDao.insert(point, "points");
    }

    public void updatePointFullScore(DAO projectDao, String pointId, double fullScore) {
        String sql = "update points set full_score = ? where point_id = ?";
        projectDao.execute(sql, pointId, fullScore);
    }
}
