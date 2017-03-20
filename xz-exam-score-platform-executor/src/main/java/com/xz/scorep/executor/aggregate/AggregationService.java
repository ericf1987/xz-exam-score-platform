package com.xz.scorep.executor.aggregate;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    public List<Row> getAggregateStatus(String projectId) {
        String sql = "select subject_id,start_time,end_time from aggregation where project_id = ? order by start_time desc";
        return this.daoFactory.getManagerDao().query(sql, projectId);
    }
}
