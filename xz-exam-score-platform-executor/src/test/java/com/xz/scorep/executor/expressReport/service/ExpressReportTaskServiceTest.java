package com.xz.scorep.executor.expressReport.service;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/12.
 */
public class ExpressReportTaskServiceTest extends BaseTest {

    @Autowired
    ExpressReportTaskService expressReportTaskService;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testDispatchOneClassOneSubject() throws Exception {
        expressReportTaskService.dispatchOneClassOneSubject(PROJECT_ID, SCHOOL_ID, CLASS_ID, SUBJECT_ID);
    }
}