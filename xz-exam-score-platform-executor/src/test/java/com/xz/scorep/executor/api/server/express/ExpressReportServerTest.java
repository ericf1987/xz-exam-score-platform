package com.xz.scorep.executor.api.server.express;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/7/7.
 */
public class ExpressReportServerTest extends BaseTest {

    @Autowired
    ExpressReportServer expressReportServer;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "317c7b47-587c-445b-83b8-c2887e51cec1";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testExecute() throws Exception {
        Param param = new Param().setParameter("projectId", PROJECT_ID)
                .setParameter("subjectId", SUBJECT_ID)
                .setParameter("schoolId", SCHOOL_ID)
                .setParameter("classId", CLASS_ID);

        Result result = expressReportServer.execute(param);
        System.out.println(result.getData().toString());
    }

}