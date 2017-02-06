package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.db.DbiHandleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private DbiHandleFactory dbiHandleFactory;

    public void clearStudents(String projectId) {
        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            handle.execute("truncate table student");
        });
    }

    public void saveStudent(String projectId, ProjectStudent student) {
        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            String sql = "insert into student(id,name,class_id) values(?,?,?)";
            handle.insert(sql, student.getId(), student.getName(), student.getClassId());
        });
    }
}
