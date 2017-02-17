package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private DAOFactory daoFactory;

    private Map<String, MultipleBatchExecutor> executorMap = new HashMap<>();

    public void clearStudents(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table student");
    }

    public void saveStudent(String projectId, ProjectStudent student) {
        String sql = "insert into student(id,name,class_id) values(?,?,?)";

        daoFactory.getProjectDao(projectId)
                .execute(sql, student.getId(), student.getName(), student.getClassId());
    }

    public MultipleBatchExecutor getMultiSaver(String projectId) {
        if (!executorMap.containsKey(projectId)) {
            executorMap.put(projectId, new MultipleBatchExecutor(
                    daoFactory.getProjectDao(projectId), 2000
            ));
        }

        return executorMap.get(projectId);
    }
}
