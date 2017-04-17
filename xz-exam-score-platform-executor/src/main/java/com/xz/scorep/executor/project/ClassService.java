package com.xz.scorep.executor.project;

import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private CacheFactory cacheFactory;

//    public void saveClass(String projectId, ProjectClass projectClass) {
//        daoFactory.getProjectDao(projectId).insert(projectClass, "class");
//    }

    public void saveClass(String projectId, List<ProjectClass> classList) {
        daoFactory.getProjectDao(projectId).insert(classList, "class");
    }

    public void saveClass(String projectId, ProjectClass projectClass) {
        String sql = "insert ignore into class(id,name,school_id,area,city,province) values(?,?,?,?,?,?)";

        daoFactory.getProjectDao(projectId).execute(sql,
                projectClass.getId(), projectClass.getName(), projectClass.getSchoolId(), projectClass.getArea(), projectClass.getCity(), projectClass.getProvince());
    }

    public void clearClasses(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table class");
    }

    public List<ProjectClass> listClasses(String projectId) {
        SimpleCache cache = cacheFactory.getProjectCache(projectId);
        String cacheKey = "classes:";

        return cache.get(cacheKey, () ->
                new ArrayList<>(daoFactory.getProjectDao(projectId)
                        .query(ProjectClass.class, "select * from class")));
    }

    public List<ProjectClass> listClasses(String projectId, String schoolId) {
        return listClasses(projectId).stream()
                .filter(c -> c.getSchoolId().equals(schoolId))
                .collect(Collectors.toList());
    }
}
