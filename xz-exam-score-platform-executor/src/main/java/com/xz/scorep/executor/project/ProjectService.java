package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ProjectStatus;
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
    public boolean projectDatabaseExists(String projectId) {
        String sql = "SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA WHERE schema_name=?";
        Row row = daoFactory.getRootDao().queryFirst(sql, projectId);
        return row != null;
    }

    // 创建项目数据库
    private void createProjectDatabase(String projectId) {

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

    // 创建保存某些统计结果的表
    private void createAggrResultTables(String projectId) {
        DAO dao = daoFactory.getProjectDao(projectId);

        // 考试项目总分表（科目总分表和题目得分表分别在 SubjectService 和 ScoreService）
        dao.execute("create table score_project(student_id VARCHAR(40) primary key,score decimal(5,1),paper_score_type varchar(6))");

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //平均分
        dao.execute("create table average_score(range_type varchar(20)," +
                "range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
                "average_score decimal(5,2))");
        dao.execute("create index idxavgscore on average_score(range_type,range_id,target_type,target_id)");

        //考试项目总分超均率....(科目超均率在SubjectService )
        dao.execute("CREATE TABLE over_average_project (range_id VARCHAR(40), range_type VARCHAR(16), target_id VARCHAR(40), target_type VARCHAR(16), over_average DECIMAL(6,4))");
        dao.execute("CREATE INDEX idxova ON over_average_project (range_id, range_type, target_id, target_type)");

        //考试项目得分率....(科目得分率在SubjectService)
        dao.execute("CREATE TABLE score_rate_project (student_id VARCHAR(40), score_level VARCHAR(10), score_rate DECIMAL(6,4))");
        dao.execute("CREATE INDEX idxsrp ON score_rate_project (student_id)");

        //最高分  最低分,科目,总分，班级,学校,项目
        dao.execute("create table max_min_score(range_type varchar(20),range_id VARCHAR(40)," +
                "target_type VARCHAR(20),target_id VARCHAR(40)," +
                "max_score decimal(5,2),min_score decimal(5,2))");
        dao.execute("create index idxmaxminscore on max_min_score(range_type,range_id,target_type,target_id)");

        //每一道题目的平均分和最高分
        dao.execute("create table quest_average_max_score(" +
                " quest_id varchar(40),quest_no varchar(20),subject_id varchar(10)," +
                " full_score decimal(4,1),average_score decimal(4,2),max_score decimal(4,2)," +
                " objective varchar(5),range_type varchar(20),range_id varchar(40))");
        dao.execute("create index idxqams on quest_average_max_score(quest_id,quest_no,range_type,range_id)");

        //客观题班级得分率
        dao.execute("create table objective_score_rate(quest_id varchar(40),quest_no varchar(20)," +
                "subject_id varchar(20),range_id varchar(40),range_type varchar(20)," +
                "answer text,score_rate decimal(5,2))");
        dao.execute("create index idxocr on objective_score_rate(quest_id,range_id,range_type)");

        //标准差
        dao.execute("create table std_deviation(range_type varchar(20),range_id VARCHAR(40)," +
                "target_type VARCHAR(20),target_id VARCHAR(40),std_deviation decimal(5,2))");
        dao.execute("create index idxstd on std_deviation(range_type,range_id,target_type,target_id)");

        //；学科T值
        dao.execute("create table t_value(range_type varchar(20),range_id varchar(40)," +
                "subject_id varchar(20),value decimal(6,2))");

        //客观题区分度 (省,学校,班级)
        dao.execute("create table distinction(quest_id varchar(40),range_type varchar(20)," +
                "range_id varchar(40),value decimal(6,2))");
        dao.execute("create index idxdsc on distinction(quest_id,range_id)");


        dao.execute("create table median(range_type varchar(20),range_id varchar(40)," +
                "target_type varchar(20),target_id varchar(40),median decimal(6,2))");
        dao.execute("create index idxm on median(range_type,range_id,target_type,target_id)");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //全科及格和全科不及格率
        dao.execute("create table all_pass_or_fail (range_type varchar(20), range_id varchar(40),all_pass_count int(11),all_pass_rate decimal(5,2),all_fail_count int(11),all_fail_rate decimal(5,2))");
        dao.execute("create index idxapfri on all_pass_or_fail(range_id)");

        // 分数分段表
        dao.execute("create table segments(" +
                "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
                "score_min decimal(5,1) not null,score_max decimal(5,1) not null,student_count int default 0 not null)");
        dao.execute("create index idxsgmtrt on segments(range_type,range_id,target_type,target_id)");

        // 得分等级表
        dao.execute("create table scorelevelmap(" +
                "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
                "score_level varchar(20),student_count int,student_rate decimal(5,2))");
        dao.execute("create index idxslm on scorelevelmap(range_type,range_id,target_type,target_id)");

        // 总体排名表
        dao.execute("create table rank_province(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrpt on rank_province(student_id)");

        // 学校排名表
        dao.execute("create table rank_school(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrst on rank_school(student_id)");

        // 班级排名表
        dao.execute("create table rank_class(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxrct on rank_class(student_id)");

        // 班级客观题排名
        dao.execute("create table rank_objective(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxotr on rank_objective(student_id)");
        // 班级主观题排名
        dao.execute("create table rank_subjective(student_id varchar(40) not null,subject_id varchar(10) not null,rank int)");
        dao.execute("create index idxstr on rank_subjective(student_id)");

        // 客观题选项选择率
        dao.execute("create table objective_option_rate(" +
                "quest_id varchar(40),`option` varchar(10)," +
                "range_type varchar(10),range_id varchar(40)," +
                "option_count int,option_rate decimal(7,4))");
        dao.execute("create index idxoorqr on objective_option_rate(quest_id,range_type,range_id)");
    }

    // 创建基础数据表
    private void createInitialTables(String projectId) {
        DAO dao = daoFactory.getProjectDao(projectId);
        dao.execute("create table school (id varchar(40) primary key, name varchar(50), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create table class  (id varchar(40) primary key, name varchar(20), school_id varchar(40), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create table student(id varchar(40) primary key, name varchar(50), exam_no varchar(20), school_exam_no varchar(20), class_id varchar(40), school_id varchar(40), area varchar(6), city varchar(6), province varchar(6))");
        dao.execute("create index idxstuc on student(class_id)");
        dao.execute("create index idxstus on student(school_id)");
        dao.execute("create table subject(id varchar(9)  primary key, name varchar(20), full_score decimal(4,1) default 0, card_id varchar(20), virtual_subject varchar(5))");
        dao.execute("create table absent (student_id varchar(40), subject_id varchar(9), PRIMARY KEY(student_id, subject_id))");
        dao.execute("create table cheat  (student_id varchar(40), subject_id varchar(9), PRIMARY KEY(student_id, subject_id))");
        dao.execute("create table lost   (student_id varchar(40), subject_id varchar(9), PRIMARY KEY(student_id, subject_id))");
        dao.execute("create table quest  (" +
                "  id varchar(40) primary key, " +
                "  exam_subject varchar(10), " +
                "  quest_subject varchar(10), " +
                "  question_type_id varchar(40), " +
                "  question_type_name varchar(10), " +
                "  objective varchar(5), " +
                "  give_full_score varchar(5), " +
                "  quest_no varchar(20), " +
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

    // 项目状态时刻在变化，这里将来也不能改成缓存
    public ExamProject findProject(String projectId) {
        String sql = "select * from project where id=?";
        return daoFactory.getManagerDao().queryFirst(ExamProject.class, sql, projectId);
    }

    public void updateProjectFullScore(String projectId, double fullScore) {
        daoFactory.getManagerDao().execute("update project set full_score=? where id=?", fullScore, projectId);
    }

    public void updateProjectStatus(String projectId, ProjectStatus status) {
        daoFactory.getManagerDao().execute("update project set status=? where id=?", status.name(), projectId);
    }

    public boolean updateProjectStatus(String projectId, ProjectStatus fromStatus, ProjectStatus toStatus) {
        return daoFactory.getManagerDao().execute(
                "update project set status=? where id=? and status=?",
                toStatus.name(), projectId, fromStatus.name()) > 0;
    }
}
