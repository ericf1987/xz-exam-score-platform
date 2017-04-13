package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 缺卷处理
 * 缺卷——记不记入统计规则同缺考，如果计入统计为0分
 */
@Service
public class LostService {
    @Autowired
    private DAOFactory daoFactory;

    public void saveLost(String projectId, String studentId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("insert ignore into lost set student_id=?,subject_id=?",
                studentId, subjectId);
    }

    public void clearLost(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table lost");
    }
}
