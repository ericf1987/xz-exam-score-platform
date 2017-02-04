package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.beans.exam.ExamProject;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactory;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    public void initProjectDatabase(String projectId) {
        DbiHandleFactory dbiHandleFactory = dbiHandleFactoryManager.getDefaultDbiHandleFactory();
        dbiHandleFactory.dropProjectDatabase(projectId);
        dbiHandleFactory.createProjectDatabase(projectId);

        DBIHandle dbiHandle = dbiHandleFactory.getProjectDBIHandle(projectId);
        createInitialTables(dbiHandle);
    }

    private void createInitialTables(DBIHandle dbiHandle) {
        dbiHandle.runHandle(handle -> {
            handle.execute("create table school (id varchar(36) primary key)");
            handle.execute("create table class  (id varchar(36) primary key, school varchar(36))");
            handle.execute("create table student(id varchar(36) primary key, class  varchar(36))");
            handle.execute("create table subject(id varchar(9)  primary key)");
            handle.execute("create table quest  (id varchar(36) primary key, questNo varchar(10), subject varchar(9), full_score decimal(4,1))");
        });
    }

    public void saveProject(ExamProject project) {
        getManagerDBIHandle().runHandle(handle -> {
            String sql = "insert into project(id, name, grade) values(?,?,?)";
            handle.insert(sql, project.getId(), project.getName(), project.getGrade());
        });
    }

    public ExamProject findProject(String projectId) {
        return getManagerDBIHandle().queryFirst(handle -> {
            String sql = "select id, name, grade from project where id=?";
            return handle.createQuery(sql).bind(0, projectId).map(ExamProject.class).first();
        });
    }

    private DBIHandle getManagerDBIHandle() {
        return dbiHandleFactoryManager.getDefaultDbiHandleFactory().getManagerDBIHandle();
    }
}
