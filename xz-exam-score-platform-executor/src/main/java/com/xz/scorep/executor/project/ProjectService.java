package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ExamProject;
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
        createAggrResultTables(projectId);
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

        String username = StringUtil.substring(projectId, 0, 32);

        DAO dao = daoFactory.getRootDao();
        dao.execute("create database `" + projectId + "` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        dao.execute("create user '" + username + "'@'%' IDENTIFIED by '" + username + "'");
        dao.execute("grant all on `" + projectId + "`.* to '" + username + "'@'%'");
        dao.execute("flush privileges");
    }

    // 删除项目数据库
    private void dropProjectDatabase(String projectId) {
        String username = StringUtil.substring(projectId, 0, 32);
        DAO dao = daoFactory.getRootDao();
        dao.execute("drop database if exists `" + projectId + "`");
        dao.execute("drop user if exists `" + username + "`");
        LOG.info("数据库 " + projectId + " 已删除。");
    }

    private void createAggrResultTables(String projectId) {
        DAO dao = daoFactory.getProjectDao(projectId);
        dao.execute("create table score_project(student_id VARCHAR(40) primary key,score decimal(5,1))");

        dao.execute("create table average_project(range_type varchar(20), range_id varchar(40), score decimal(4,1))");
        dao.execute("create index idxavgpri on average_project(range_id)");
        dao.execute("create table average_subject(range_type varchar(20), range_id varchar(40), subject_id varchar(20), score decimal(4,1))");
        dao.execute("create index idxavgsri on average_subject(range_id)");
        dao.execute("create table average_quest(range_type varchar(20), range_id varchar(40), quest_id varchar(40), score decimal(4,1))");
        dao.execute("create index idxavgqri on average_quest(range_id)");

        dao.execute("create table segments(" +
                "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
                "score_min decimal(5,1) not null,score_max decimal(5,1) not null,student_count int default 0 not null)");
        dao.execute("create index idxsgmtrt on segments(range_type,range_id,target_type,target_id)");

        dao.execute("create table scorelevelmap(" +
                "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
                "score_level varchar(20),student_count int,student_rate decimal(5,2))");
        dao.execute("create index idxslm on scorelevelmap(range_type,range_id,target_type,target_id)");

        dao.execute("create table rank_province(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrpt on rank_province(student_id)");

        dao.execute("create table rank_school(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrst on rank_school(student_id)");

        dao.execute("create table rank_class(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrct on rank_class(student_id)");
    }

    private void createInitialTables(String projectId) {
        DAO dao = daoFactory.getProjectDao(projectId);
        dao.execute("create table school (id varchar(40) primary key, name varchar(50), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create table class  (id varchar(40) primary key, name varchar(20), school_id varchar(40), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create table student(id varchar(40) primary key, name varchar(50), exam_no varchar(20), school_exam_no varchar(20), class_id varchar(40), school_id varchar(40), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create index idxstuc on student(class_id)");
        dao.execute("create index idxstus on student(school_id)");
        dao.execute("create table subject(id varchar(9)  primary key, name varchar(20), full_score decimal(4,1) default 0, card_id varchar(20))");
        dao.execute("create table absent (student_id varchar(40), subject_id varchar(9))");
        dao.execute("create table cheat  (student_id varchar(40), subject_id varchar(9))");
        dao.execute("create table quest  (" +
                "  id varchar(40) primary key, " +
                "  exam_subject varchar(10), " +
                "  quest_subject varchar(10), " +
                "  question_type_id varchar(40), " +
                "  question_type_name varchar(10), " +
                "  objective varchar(5), " +
                "  give_full_score varchar(5), " +
                "  quest_no varchar(10), " +
                "  full_score decimal(4,1), " +
                "  answer text, " +
                "  score_rule varchar(100), " +
                "  options text" +
                ")");

        dao.execute("CREATE TABLE quest_point (" +
                "  quest_id varchar(40) NOT NULL," +
                "  point_id varchar(20) NOT NULL," +
                "  point_name varchar(100) NOT NULL)");
    }

    //////////////////////////////////////////////////////////////

    public void saveProject(ExamProject project) {
        DAO managerDao = daoFactory.getManagerDao();
        managerDao.delete(project, "project");
        managerDao.insert(project, "project");
    }

    public ExamProject findProject(String projectId) {
        String sql = "select * from project where id=?";
        return daoFactory.getManagerDao().queryFirst(ExamProject.class, sql, projectId);
    }

    public String getProjectProvince(String projectId) {
        return "430000";
    }
}
