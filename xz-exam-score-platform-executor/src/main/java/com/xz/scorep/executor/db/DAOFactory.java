package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.hyd.dao.DAO;
import com.hyd.dao.DataSources;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.AggregateStatus;
import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.config.DbConfig;
import com.xz.scorep.executor.utils.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * DAO 对象工厂类
 */
@Component
public class DAOFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DAOFactory.class);

    private static final String DS_ROOT = "root";

    private static final String DS_MANAGER = "manager";

    @Autowired
    private DbConfig dbConfig;

    private DataSources dataSources = new DataSources();

    @PostConstruct
    public void init() {
        // 预定义数据库
        dataSources.setDataSource(DS_ROOT, getRootDataSource());
        dataSources.setDataSource(DS_MANAGER, getManagerDataSource());

        resetProjectStatus();
    }

    private void resetProjectStatus() {

        // 执行单元测试时不修改项目状态
        if (SysUtils.isUnitTesting()) {
            return;
        }

        LOG.info("恢复项目状态...");
        getManagerDao().execute("update project set status=? where status<>?",
                ProjectStatus.Ready.name(), ProjectStatus.Ready.name());

        LOG.info("恢复统计任务状态...");
        getManagerDao().execute(
                "update aggregation set status=? where status=?",
                AggregateStatus.Finished.name(), AggregateStatus.Running.name());

    }

    // 获得 root 连接
    public DAO getRootDao() {
        return dataSources.getDAO(DS_ROOT);
    }

    // 获得管理库连接
    public DAO getManagerDao() {
        DAO dao = dataSources.getDAO(DS_MANAGER);
        if (dao == null) {
            throw new IllegalStateException("Manager DAO is null");
        }
        return dao;
    }

    // 获得项目库连接 DAO
    public synchronized DAO getProjectDao(String projectId) {

        if (!dataSources.contains(projectId)) {
            dataSources.setDataSource(projectId, getProjectDataSource(projectId));
        }
        return dataSources.getDAO(projectId);
    }

    private synchronized DataSource getRootDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl("information_schema"));
        dataSource.setUsername(dbConfig.getUser());
        dataSource.setPassword(dbConfig.getPass());
        return dataSource;
    }

    private synchronized DataSource getManagerDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getManagerUrl());
        dataSource.setUsername(dbConfig.getManagerUser());
        dataSource.setPassword(dbConfig.getManagerPass());
        return dataSource;
    }

    private synchronized DataSource getProjectDataSource(String projectId) {
        String username = StringUtil.substring(projectId, 15);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl(projectId));
        dataSource.setUsername(username);
        dataSource.setPassword(username);
        dataSource.setMaxActive(dbConfig.getPoolSize());
        return dataSource;
    }

}
