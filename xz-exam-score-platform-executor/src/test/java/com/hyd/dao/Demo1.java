package com.hyd.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.xz.scorep.executor.BaseTest;

import java.util.ResourceBundle;

/**
 * (description)
 * created at 2017/2/27
 *
 * @author yidin
 */
public class Demo1 {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("application");

    static {
        BaseTest.setupProxy();
    }

    public static void main(String[] args) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://10.10.22.212:3306/fake_project_small?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false");
        dataSource.setUsername("fake_project_small");
        dataSource.setPassword("fake_project_small");

        DataSources dataSources = new DataSources();
        dataSources.setDataSource("manager", dataSource);
        dataSources.setDataSource("manager", dataSource);
        dataSources.setDataSource("manager", dataSource);
        dataSources.setDataSource("manager", dataSource);
        dataSources.setDataSource("manager", dataSource);

        DAO dao1 = dataSources.getDAO("manager");
        DAO dao2 = dataSources.getDAO("manager2");
        DAO dao3 = dataSources.getDAO("manager3");

        Runnable runnable = () -> {
            dao1.execute(SQL.Update("student").Set("name=?", "张三").Where("exam_no=?", "000000"));
            dao2.execute("insert into student() values()");
            dao3.execute("insert into student() values()");
        };

        DAO.runTransactionWithException(runnable);
    }
}
