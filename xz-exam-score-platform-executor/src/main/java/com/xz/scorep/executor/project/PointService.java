package com.xz.scorep.executor.project;

import com.hyd.dao.BatchCommand;
import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.DoubleCounterMap;
import com.xz.scorep.executor.bean.Point;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void batchUpdateFullScore(DAO projectDao, DoubleCounterMap<String> pointFullScore) {
        BatchCommand batchCommand = new BatchCommand("update points set full_score = ? where point_id = ?");
        for (String pointId : pointFullScore.keySet()) {
            double fullScore = pointFullScore.get(pointId);
            batchCommand.addParams(fullScore, pointId);
        }
        projectDao.execute(batchCommand);
    }
}
