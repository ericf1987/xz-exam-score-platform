package com.xz.scorep.executor.exportaggrdata.bean;

import com.hyd.simplecache.utils.MD5;

import java.util.UUID;

/**
 * mongo统计对象
 * @author by fengye on 2017/7/17.
 */
public class MongoAggrObject {

    private String project;

    private String md5;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setAggrObject(MongoAggrObject mongoAggrObject, String projectId){
        mongoAggrObject.setProject(projectId);
        mongoAggrObject.setMd5(MD5.digest(UUID.randomUUID().toString()));
    }
}
