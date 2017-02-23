package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.hyd.dao.DAO;
import com.hyd.dao.DataSources;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.config.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * DAO 对象工厂类
 */
@Component
public class DAOFactory {

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
        String username = StringUtil.substring(projectId, 0, 32);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl(projectId));
        dataSource.setUsername(username);
        dataSource.setPassword(username);
        return dataSource;
    }

}
