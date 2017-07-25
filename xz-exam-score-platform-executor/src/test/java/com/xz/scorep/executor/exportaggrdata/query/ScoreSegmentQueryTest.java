package com.xz.scorep.executor.exportaggrdata.query;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreSegment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author by fengye on 2017/7/25.
 */
public class ScoreSegmentQueryTest extends BaseTest {

    @Autowired
    ScoreSegmentsQuery scoreSegmentsQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    @Test
    public void testQueryObj() throws Exception {
        List<ScoreSegment> scoreSegments = scoreSegmentsQuery.queryObj(PROJECT_ID);
        System.out.println(scoreSegments);
    }
}