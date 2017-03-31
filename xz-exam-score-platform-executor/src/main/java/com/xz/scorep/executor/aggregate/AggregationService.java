package com.xz.scorep.executor.aggregate;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AggregationService {

    @Autowired
    private DAOFactory daoFactory;

    public void insertAggregation(Aggregation aggregation) {
        DAO managerDao = this.daoFactory.getManagerDao();
        managerDao.delete(aggregation, "aggregation");
        managerDao.insert(aggregation, "aggregation");
    }

    public void endAggregation(String aggregationId, AggregateStatus status, Date endTime) {
        this.daoFactory.getManagerDao().execute(
                SQL.Update("aggregation")
                        .Set("status", status.name()).Set("end_time", endTime)
                        .Where("id=?", aggregationId));
    }

    public Row getAggregateStatus(String projectId) {
        String sql = "select * from aggregation where project_id = ? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId);
    }

    public Row getAggregateByStatus(String projectId, String status) {
        String sql = "select * from aggregation where project_id = ? and status = ? order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, status);
    }


    public Row getAggregateStatus(String projectId, String subjectId) {
        String sql = "select * from aggregation where project_id = ? and subject_id =? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, subjectId);
    }


    public Row getAggregateStatus(String projectId, AggregateType aggrType) {
        String sql = "select * from aggregation where project_id = ? and aggr_type =?  and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, aggrType.name());
    }

    public Row getAggregateStatus(String projectId, AggregateType aggrType, String subjectId) {
        String sql = "select * from aggregation where project_id = ? and aggr_type =? and subject_id =? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, aggrType.name(), subjectId);
    }


}
