package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.db.DAOFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/28
 *
 * @author yidin
 */
public class RankAggregatorTest extends BaseTest {

    @Autowired
    private RankAggregator rankAggregator;

    @Autowired
    DAOFactory daoFactory;

    @Test
    public void aggregate() throws Exception {
        rankAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }

    @Test
    public void testAggregateClassSubjectRank() throws Exception {
        String classId = "2a519910-5a86-492a-90cb-0c65d4c3b647";
        DAO projectDao = daoFactory.getProjectDao(PROJECT_ID);

        projectDao.execute("delete from rank_class" +
                " where subject_id='001' and " +
                " student_id in(select id from student where class_id=?)", classId);

        String rangeTemplate = RankAggregator.RANGE_CLASS_TEMPLATE.replace("{{class}}", classId);
        rankAggregator.aggregateClassSubjectRank(projectDao, rangeTemplate, "001");
    }
}