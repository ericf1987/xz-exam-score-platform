package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private DAOFactory daoFactory;

    public void clearStudents(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table student");
    }

    public void saveStudent(String projectId, ProjectStudent student) {
        String sql = "insert into student(id,name,class_id) values(?,?,?)";

        daoFactory.getProjectDao(projectId)
                .execute(sql, student.getId(), student.getName(), student.getClassId());
    }
}
