package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassService {

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    public void saveClass(String projectId, ProjectClass projectClass) {
        getProjectDBIHandle(projectId).runHandle(handle -> {
            String sql = "insert into class(id, name, school_id) values(?,?,?)";
            handle.insert(sql, projectClass.getId(), projectClass.getName(), projectClass.getSchoolId());
        });
    }

    public void clearClasses(String projectId) {
        getProjectDBIHandle(projectId).runHandle(
                handle -> handle.execute("truncate table class"));
    }

    private DBIHandle getProjectDBIHandle(String projectId) {
        return dbiHandleFactoryManager.getDefaultDbiHandleFactory().getProjectDBIHandle(projectId);
    }
}
