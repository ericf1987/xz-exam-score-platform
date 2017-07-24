package com.xz.scorep.executor.exportaggrdata.utils;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by fengye on 2017/7/21.
 */
public class QueryTaskThread extends Thread{

    private DAO dao;

    private String sql;

    private List<Object> param;

    private List<Row> result = new ArrayList<>();

    public DAO getDao() {
        return dao;
    }

    public void setDao(DAO dao) {
        this.dao = dao;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Row> getResult() {
        return result;
    }

    public QueryTaskThread(DAO dao, String sql, List<Object> param){
        this.dao = dao;
        this.sql = sql;
        this.param = param;
    }

    @Override
    public void run() {
        List<Row> result = this.dao.query(sql, param);
        this.getResult().addAll(result);
    }
}
