package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.DoubleCounterMap;
import com.xz.scorep.executor.bean.PointLevel;
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
}
