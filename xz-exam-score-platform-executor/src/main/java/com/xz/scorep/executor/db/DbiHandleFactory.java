package com.xz.scorep.executor.db;

/**
 * (description)
 * created at 16/12/28
 *
 * @author yidin
 */
public interface DbiHandleFactory {

    // 创建项目数据库
    void createProjectDatabase(String projectId);

    // 删除项目数据库
    void dropProjectDatabase(String projectId);

    // 获得项目数据库访问对象
    DBIHandle getProjectDBIHandle(String projectId);

    // 是否启用
    boolean isEnabled();
}
