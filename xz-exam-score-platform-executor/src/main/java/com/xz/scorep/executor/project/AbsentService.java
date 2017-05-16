package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbsentService {

    @Autowired
    private DAOFactory daoFactory;

    public void saveAbsent(String projectId, String studentId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("insert ignore into absent set student_id=?,subject_id=?",
                studentId, subjectId);
    }

    public void clearAbsent(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table absent");
    }

    public List<Row> queryAbsent(String projectId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = "select student_id from absent where subject_id=?";
        return projectDao.query(sql, subjectId);
    }

    public int queryAbsentCount(String projectId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = "select count(1) from absent where subject_id=?";
        return projectDao.count(sql, subjectId);
    }

}
