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

    public Row getAggregateStatus(String projectId) {
        String sql = "select start_time,end_time from aggregation where project_id = ?";
        List<Row> list = this.daoFactory.getManagerDao().query(sql, projectId);
        if (list.isEmpty() || list.size() == 0) {
            Row row = new Row();
            row.put("start_time", "0");
            row.put("end_time", "0");
            return row;
        } else {
            return list.get(list.size() - 1);
        }
    }
}
