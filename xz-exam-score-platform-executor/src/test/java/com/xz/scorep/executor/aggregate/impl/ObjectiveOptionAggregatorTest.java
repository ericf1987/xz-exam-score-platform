package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.db.DAOFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * (description)
 * created at 2017/3/6
 *
 * @author yidin
 */
public class ObjectiveOptionAggregatorTest extends BaseTest {

    public static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Autowired
    private ObjectiveOptionAggregator aggregator;

    @Autowired
    private DAOFactory daoFactory;

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }

    @Test
    public void testBatchInsert() throws Exception {
        Map<String, Object> row = new HashMap<>();
        row.put("quest_id", "quest1");
        row.put("option", "A");
        row.put("range_type", "province");
        row.put("range_id", "430000");
        row.put("option_count", 100);
        row.put("option_rate", 0.333333);

        DAO projectDao = daoFactory.getProjectDao(PROJECT_ID);
        projectDao.insert(row, "objective_option_rate");
    }
}