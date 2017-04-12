package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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

        String countSql = "select count(1) from lost where student_id=? and subject_id=?";
        int count = projectDao.count(countSql, studentId, subjectId);

        if (count == 0) {
            Map<String, String> map = MapBuilder.start("student_id", studentId).and("subject_id", subjectId).get();
            projectDao.insert(map, "lost");
        }
    }
}
