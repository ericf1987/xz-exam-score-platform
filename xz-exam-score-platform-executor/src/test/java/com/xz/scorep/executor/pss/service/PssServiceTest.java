package com.xz.scorep.executor.pss.service;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/5/24.
 */
public class PssServiceTest extends BaseTest {

    @Autowired
    PssService pssService;

    public static final String PROJECT_ID = "430100-62fb00af4f04407e9e4383aa7cd4fdf0";

    public static final String SUBJECT_ID = "005";

    public static final String STUDENT_ID = "00ba4a1e-5be0-4ce4-95d5-b44d9f219913";

    @Test
    public void testGetOneStudentOnePage() throws Exception {
        String imgString = pssService.getOneStudentOnePage(PROJECT_ID, SUBJECT_ID, STUDENT_ID, true, null);
        System.out.println(imgString);
    }
}