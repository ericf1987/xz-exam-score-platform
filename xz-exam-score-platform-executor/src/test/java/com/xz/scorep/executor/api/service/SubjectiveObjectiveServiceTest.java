package com.xz.scorep.executor.api.service;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/1.
 */
public class SubjectiveObjectiveServiceTest extends BaseTest {

    @Autowired
    SubjectiveObjectiveService subjectiveObjectiveService;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    public static final String SUBJECT_ID = "005";

    public static final String STUDENT_ID = "b3c54b01-9f72-4192-889f-6ac293a24a57";

    public static final String CLASS_ID = "317c7b47-587c-445b-83b8-c2887e51cec1";

    @Test
    public void testQuerySubjectiveTop5() throws Exception {
        List<Map<String, Object>> list = subjectiveObjectiveService.querySubjectiveTop5(PROJECT_ID, SUBJECT_ID, STUDENT_ID, CLASS_ID);
        System.out.println(list.toString());
    }
}