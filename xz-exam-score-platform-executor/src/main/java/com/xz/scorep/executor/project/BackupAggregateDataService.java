package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 快速(Quick)统计之后备份相关数据
 *
 * @author luckylo
 * @createTime 2017-07-11.
 */
@Service
public class BackupAggregateDataService {

    private static final Logger LOG = LoggerFactory.getLogger(BackupAggregateDataService.class);

    @Autowired
    private DAOFactory daoFactory;


    public void copyOriginDataToBackupDataBase(String projectId, String subjectId) {
        LOG.info("项目ID {} ,科目ID {} 准备开始备份.....", projectId, subjectId);
        //创建备份库
        createBackDataBase(projectId, subjectId);
        //复制数据
        copyAggregateData(projectId, subjectId);
        LOG.info("项目ID {} ,科目ID {} 备份结束.....", projectId, subjectId);
    }

    //创建备份库
    private void createBackDataBase(String projectId, String subjectId) {
        initBackDataBase(projectId, subjectId);
        initTables(projectId, subjectId);
    }

    //初始化数据库
    private void initBackDataBase(String projectId, String subjectId) {
        DAO rootDao = daoFactory.getRootDao();

        String dataBaseName = projectId + "_" + subjectId + "_bak";
        String username = StringUtil.substring(projectId, 0, 32);

        rootDao.execute("drop database if exists `" + dataBaseName + "`");
        rootDao.execute("drop user if exists `" + username + "`");

        rootDao.execute("create database `" + dataBaseName + "` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        rootDao.execute("create user '" + username + "'@'%' IDENTIFIED by '" + username + "'");
        rootDao.execute("grant all on `" + dataBaseName + "`.* to '" + username + "'@'%'");
        rootDao.execute("flush privileges");
    }

    //初始化pdf数据相关表
    private void initTables(String projectId, String subjectId) {
        String dataBaseName = projectId + "_" + subjectId + "_bak";

        DAO rootDao = daoFactory.getRootDao();
        copyProjectQuest(projectId, subjectId, dataBaseName);

        rootDao.execute("create table `" + dataBaseName + "`.student like `" + projectId + "`.student");
        rootDao.execute("create table `" + dataBaseName + "`.class like `" + projectId + "`.class");
        rootDao.execute("create table `" + dataBaseName + "`.school like `" + projectId + "`.school");

        rootDao.execute("create table `" + dataBaseName + "`.subject like `" + projectId + "`.subject");

        rootDao.execute("create table `" + dataBaseName + "`.score_subject_" + subjectId + " like `" + projectId + "`.score_subject_" + subjectId);
        rootDao.execute("create table `" + dataBaseName + "`.score_objective_" + subjectId + " like `" + projectId + "`.score_objective_" + subjectId);
        rootDao.execute("create table `" + dataBaseName + "`.score_subjective_" + subjectId + " like `" + projectId + "`.score_subjective_" + subjectId);

        rootDao.execute("create table `" + dataBaseName + "`.absent like `" + projectId + "`.absent");
        rootDao.execute("create table `" + dataBaseName + "`.cheat like `" + projectId + "`.cheat");
        rootDao.execute("create table `" + dataBaseName + "`.lost like `" + projectId + "`.lost");

        rootDao.execute("create table `" + dataBaseName + "`.rank_province like `" + projectId + "`.rank_province");
        rootDao.execute("create table `" + dataBaseName + "`.rank_school like `" + projectId + "`.rank_school");
        rootDao.execute("create table `" + dataBaseName + "`.rank_class like `" + projectId + "`.rank_class");
        rootDao.execute("create table `" + dataBaseName + "`.rank_subjective like `" + projectId + "`.rank_subjective");
        rootDao.execute("create table `" + dataBaseName + "`.rank_objective like `" + projectId + "`.rank_objective");

        rootDao.execute("create table `" + dataBaseName + "`.score_project like `" + projectId + "`.score_project");
        rootDao.execute("create table `" + dataBaseName + "`.objective_score_rate like `" + projectId + "`.objective_score_rate");
        rootDao.execute("create table `" + dataBaseName + "`.quest_average_max_score like `" + projectId + "`.quest_average_max_score");

        //每个题目的得分明细....
        createQuestScoreTable(projectId, subjectId, dataBaseName);

    }

    //复制项目题目信息(特定的科目题目)
    private void copyProjectQuest(String projectId, String subjectId, String dataBaseName) {
        DAO rootDao = daoFactory.getRootDao();
        rootDao.execute("create table `" + dataBaseName + "`.quest like `" + projectId + "`.quest");
        rootDao.execute("insert into `" + dataBaseName + "`.quest select * from `" + projectId + "`.quest where exam_subject = '" + subjectId + "'");
    }

    //创建题目得分表
    private void createQuestScoreTable(String projectId, String subjectId, String dataBaseName) {
        DAO rootDao = daoFactory.getRootDao();
        List<Row> rows = rootDao.query("select * from `" + dataBaseName + "`.quest");
        rows.forEach(row -> {
            String tableName = "`score_" + row.getString("id") + "`";
            rootDao.execute("create table `" + dataBaseName + "`." + tableName + " like `" + projectId + "`." + tableName);
        });
    }

    //复制相关统计数据
    private void copyAggregateData(String projectId, String subjectId) {
        String dataBaseName = projectId + "_" + subjectId + "_bak";

        DAO rootDao = daoFactory.getRootDao();

        rootDao.execute("insert into `" + dataBaseName + "`.student select * from `" + projectId + "`.student");
        rootDao.execute("insert into `" + dataBaseName + "`.class select * from `" + projectId + "`.class");
        rootDao.execute("insert into `" + dataBaseName + "`.school select * from `" + projectId + "`.school");

        rootDao.execute("insert into `" + dataBaseName + "`.subject select * from `" + projectId + "`.subject");

        rootDao.execute("insert into `" + dataBaseName + "`.score_subject_" + subjectId + " select * from `" + projectId + "`.score_subject_" + subjectId);
        rootDao.execute("insert into `" + dataBaseName + "`.score_objective_" + subjectId + " select * from `" + projectId + "`.score_objective_" + subjectId);
        rootDao.execute("insert into `" + dataBaseName + "`.score_subjective_" + subjectId + " select * from `" + projectId + "`.score_subjective_" + subjectId);

        rootDao.execute("insert into `" + dataBaseName + "`.absent select * from `" + projectId + "`.absent");
        rootDao.execute("insert into `" + dataBaseName + "`.lost select * from `" + projectId + "`.lost");
        rootDao.execute("insert into `" + dataBaseName + "`.cheat select * from `" + projectId + "`.cheat");

        rootDao.execute("insert into `" + dataBaseName + "`.rank_province select * from `" + projectId + "`.rank_province");
        rootDao.execute("insert into `" + dataBaseName + "`.rank_school  select * from `" + projectId + "`.rank_school");
        rootDao.execute("insert into `" + dataBaseName + "`.rank_class  select * from `" + projectId + "`.rank_class");
        rootDao.execute("insert into `" + dataBaseName + "`.rank_subjective select * from `" + projectId + "`.rank_subjective");
        rootDao.execute("insert into `" + dataBaseName + "`.rank_objective  select * from `" + projectId + "`.rank_objective");

        rootDao.execute("insert into `" + dataBaseName + "`.score_project select * from `" + projectId + "`.score_project");
        rootDao.execute("insert into `" + dataBaseName + "`.objective_score_rate select * from `" + projectId + "`.objective_score_rate");
        rootDao.execute("insert into `" + dataBaseName + "`.quest_average_max_score select * from `" + projectId + "`.quest_average_max_score");

        //backupDao.execute("insert into * select * from `" + projectId + "`.*");
        copyQuestScore(projectId, rootDao, dataBaseName);
    }

    //复制每一道题目分数
    private void copyQuestScore(String projectId, DAO rootDao, String dataBaseName) {
        List<Row> rows = rootDao.query("select * from `" + dataBaseName + "`.quest");
        rows.forEach(row -> {
            String tableName = "`score_" + row.getString("id") + "`";
            rootDao.execute("insert into `" + dataBaseName + "`." + tableName + " select * from `" + projectId + "`." + tableName);
        });
    }
}
