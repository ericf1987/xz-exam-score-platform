package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubjectService {

    @Autowired
    private DAOFactory daoFactory;

    public void clearSubjects(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table subject");
    }

    public void saveSubject(String projectId, String subjectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String querySubject = "select * from subject where id=?";
        Row row = projectDao.queryFirst(querySubject, subjectId);

        if (row == null) {
            projectDao.execute("insert into subject(id) values(?)", subjectId);
        }
    }
}
