package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.DoubleCounterMap;
import com.xz.scorep.executor.bean.PointLevel;
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
public class PointLevelService {

    @Autowired
    private DAOFactory daoFactory;

    public void batchUpdateFullScore(DAO projectDao, DoubleCounterMap<PointLevel> pointLevelFullScore) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (PointLevel pointLevel : pointLevelFullScore.keySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("point", pointLevel.getPoint());
            m.put("level", pointLevel.getLevel());
            m.put("full_score", pointLevelFullScore.get(pointLevel));
            resultList.add(m);
        }

        projectDao.insert(resultList, "point_level");
    }

    public List<Row> listPointLevels(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.query("select * from point_level");
    }
}
