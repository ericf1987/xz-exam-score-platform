package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.beans.exam.ExamProject;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private DAOFactory daoFactory;

    public void initProjectDatabase(String projectId) {
        dropProjectDatabase(projectId);
        createProjectDatabase(projectId);
        createInitialTables(projectId);
    }

    // 检查数据库是否存在
    private boolean projectDatabaseExists(String projectId) {
        String sql = "SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA WHERE schema_name=?";
        Row row = daoFactory.getRootDao().queryFirst(sql, projectId);
        return row != null;
    }

    // 创建项目数据库
    private void createProjectDatabase(String projectId) {

        if (projectDatabaseExists(projectId)) {
            dropProjectDatabase(projectId);
        }

        DAO dao = daoFactory.getRootDao();
        dao.execute("create database " + projectId + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        dao.execute("create user '" + projectId + "'@'%' IDENTIFIED by '" + projectId + "'");
        dao.execute("grant all on " + projectId + ".* to '" + projectId + "'@'%'");
        dao.execute("flush privileges");
    }

    // 删除项目数据库
    private void dropProjectDatabase(String projectId) {
        DAO dao = daoFactory.getRootDao();
        dao.execute("drop database if exists " + projectId);
        dao.execute("drop user if exists " + projectId);
        LOG.info("数据库 " + projectId + " 已删除。");
    }

    private void createInitialTables(String projectId) {
        DAO dao = daoFactory.getProjectDao(projectId);
        dao.execute("create table school (id varchar(36) primary key, name varchar(50), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create table class  (id varchar(36) primary key, name varchar(20), school_id varchar(36))");
        dao.execute("create table student(id varchar(36) primary key, name varchar(50), class_id varchar(36))");
        dao.execute("create table subject(id varchar(9)  primary key)");
        dao.execute("create table quest  (" +
                "  id varchar(36) primary key, " +
                "  exam_subject varchar(10), " +
                "  quest_subject varchar(10), " +
                "  question_type_id varchar(36), " +
                "  question_type_name varchar(10), " +
                "  objective varchar(5), " +
                "  quest_no varchar(10), " +
                "  full_score decimal(4,1), " +
                "  answer text, " +
                "  score_rule varchar(100), " +
                "  options text" +
                ")");
    }

    public void saveProject(ExamProject project) {
        String sql = "insert into project(id, name, grade) values(?,?,?)";
        daoFactory.getManagerDao().execute(sql, project.getId(), project.getName(), project.getGrade());
    }

    public ExamProject findProject(String projectId) {
        String sql = "select id, name, grade from project where id=?";
        return daoFactory.getManagerDao().queryFirst(ExamProject.class, sql, projectId);
    }
}
