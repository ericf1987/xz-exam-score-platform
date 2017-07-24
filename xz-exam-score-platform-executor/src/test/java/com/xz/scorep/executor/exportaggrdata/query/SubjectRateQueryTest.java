package com.xz.scorep.executor.exportaggrdata.query;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.exportaggrdata.bean.SubjectRate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/24.
 */
public class SubjectRateQueryTest extends BaseTest {

    @Autowired
    SubjectRateQuery subjectRateQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    @Test
    public void testQueryObj() throws Exception {
        List<SubjectRate> subjectRates = subjectRateQuery.queryObj(PROJECT_ID);
        System.out.println(subjectRates);
    }
}