package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private CacheFactory cacheFactory;

    private Map<String, MultipleBatchExecutor> executorMap = new HashMap<>();

    public void clearStudents(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table student");
    }

    public void saveStudent(String projectId, ProjectStudent student) {
        daoFactory.getProjectDao(projectId).insert(student, "student");
    }

    public void saveStudent(String projectId, List<ProjectStudent> studentList) {
        daoFactory.getProjectDao(projectId).insert(studentList, "student");
    }

    public MultipleBatchExecutor getMultiSaver(String projectId) {
        if (!executorMap.containsKey(projectId)) {
            executorMap.put(projectId, new MultipleBatchExecutor(
                    daoFactory.getProjectDao(projectId), 2000
            ));
        }

        return executorMap.get(projectId);
    }

    public void cacheStudents(String projectId) {
        SimpleCache projectCache = cacheFactory.getProjectCache(projectId);
        daoFactory.getProjectDao(projectId).query("select * from student").forEach(row -> {
            String studentId = row.getString("id");
            projectCache.put("student:" + studentId, row);
        });
    }

    public Row findStudent(String projectId, String studentId) {
        String cacheKey = "student:" + studentId;

        return cacheFactory.getProjectCache(projectId).get(cacheKey, () -> {
            String sql = "select * from student where id=?";
            return daoFactory.getProjectDao(projectId).queryFirst(sql, studentId);
        });
    }

    public List<String> listStudents(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.query("select * from student")
                .stream()
                .map(row -> row.getString("id"))
                .collect(Collectors.toList());
    }
}
