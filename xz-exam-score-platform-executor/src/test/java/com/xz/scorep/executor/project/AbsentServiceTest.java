package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/4/28.
 */
public class AbsentServiceTest extends BaseTest {

    @Autowired
    AbsentService absentService;

    final String PROJECT_ID = "431100-c2bd703d34c440d4ad98f4404cd0526e";

    final String SUBJECT_ID = "006";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSaveAbsent() throws Exception {

    }

    @Test
    public void testClearAbsent() throws Exception {

    }

    @Test
    public void testQueryAbsent() throws Exception {
        absentService.queryAbsent(PROJECT_ID, SUBJECT_ID);
    }

    @Test
    public void testQueryAbsentCount() throws Exception {
        System.out.println(absentService.queryAbsentCount(PROJECT_ID, SUBJECT_ID));
    }
}