package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassService {

    @Autowired
    private DAOFactory daoFactory;

    public void saveClass(String projectId, ProjectClass projectClass) {
        daoFactory.getProjectDao(projectId).insert(projectClass, "class");
    }

    public void clearClasses(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table class");
    }

    public List<ProjectClass> listClasses(String projectId, String schoolId) {
        return daoFactory.getProjectDao(projectId).query(
                ProjectClass.class, "select * from class where school_id=?", schoolId);
    }

    public List<ProjectClass> listClasses(String projectId) {
        return daoFactory.getProjectDao(projectId).query(
                ProjectClass.class, "select * from class");
    }
}
