package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.bean.PointLevel;
import org.springframework.stereotype.Service;

/**
 * @author by fengye on 2017/6/26.
 */
@Service
public class PointLevelService {

    public void updatePointLevelFullScore(DAO projectDao, PointLevel pointLevel, double score) {
        String sql = "update point_level set full_score where level_id = ? and point_id = ?";
        projectDao.execute(sql, pointLevel.getLevel(), pointLevel.getPoint());
    }
}
