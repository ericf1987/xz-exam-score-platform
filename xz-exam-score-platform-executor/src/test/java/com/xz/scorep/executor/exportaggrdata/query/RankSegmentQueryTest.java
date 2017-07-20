package com.xz.scorep.executor.exportaggrdata.query;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.exportaggrdata.bean.RankSegment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/20.
 */
public class RankSegmentQueryTest extends BaseTest {

    @Autowired
    RankSegmentQuery rankSegmentQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    @Test
    public void testQueryObj() throws Exception {
        rankSegmentQuery.queryObj(PROJECT_ID);
    }
}