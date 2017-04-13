package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheatService {

    @Autowired
    private DAOFactory daoFactory;

    public void saveCheat(String projectId, String studentId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("insert ignore into cheat set student_id=?,subject_id=?",
                studentId, subjectId);
    }

    public void clearCheat(String projectId){
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table cheat");
    }
}
