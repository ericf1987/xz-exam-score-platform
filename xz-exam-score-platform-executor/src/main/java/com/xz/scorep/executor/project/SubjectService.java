package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void createSubjectScoreTable(String projectId, String subjectId) {

        String tableName = "score_subject_" + subjectId;

        String createSubjectTable = "create table " + tableName +
                "(student_id VARCHAR(36) primary key,score decimal(4,1))";

        daoFactory.getProjectDao(projectId).execute(createSubjectTable);
    }

    public List<ExamSubject> listSubjects(String projectId) {
        return daoFactory.getProjectDao(projectId).query(ExamSubject.class, "select * from subject");
    }
}
