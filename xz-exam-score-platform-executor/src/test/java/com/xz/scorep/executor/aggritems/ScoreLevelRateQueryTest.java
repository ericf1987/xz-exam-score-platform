package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class ScoreLevelRateQueryTest extends BaseTest {

    @Autowired
    private ScoreLevelRateQuery scoreLevelRateQuery;

    @Test
    public void testGetSchoolProjectSLR() throws Exception {
        Row row = scoreLevelRateQuery.getSchoolProjectSLR(PROJECT_ID, SCHOOL_ID);
        System.out.println(row);
    }

    @Test
    public void testGetClassProjectSLRs() throws Exception {
        List<Row> classProjectSLRs = scoreLevelRateQuery.getClassProjectSLRs(PROJECT_ID, SCHOOL_ID);
        classProjectSLRs.forEach(System.out::println);
    }
}