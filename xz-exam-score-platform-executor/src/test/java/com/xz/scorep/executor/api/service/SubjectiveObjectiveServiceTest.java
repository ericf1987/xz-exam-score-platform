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

    public static final String PROJECT_ID = "430100-258c4700b34c4842812f1066b3acdf77";

    public static final String SUBJECT_ID = "003";

    public static final String STUDENT_ID = "838d0c22-07ba-4101-9cab-a97fc083327e";

    public static final String CLASS_ID = "c7f72f73-1520-4004-a33d-1cc9bf28a783";

    @Test
    public void testQuerySubjectiveTop5() throws Exception {
        List<Map<String, Object>> list = subjectiveObjectiveService.querySubjectiveTop5(PROJECT_ID, SUBJECT_ID, STUDENT_ID, CLASS_ID);
        System.out.println(list.toString());
    }

    @Test
    public void testQuerySubjectiveScoreDetail() throws Exception {
        List<Map<String, Object>> list = subjectiveObjectiveService.querySubjectiveScoreDetail(PROJECT_ID, SUBJECT_ID, CLASS_ID, STUDENT_ID);
        System.out.println(list.toString());
    }
}