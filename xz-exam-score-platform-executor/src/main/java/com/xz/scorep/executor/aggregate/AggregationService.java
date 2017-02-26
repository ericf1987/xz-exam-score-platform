package com.xz.scorep.executor.aggregate;

import com.hyd.dao.DAO;
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
}
