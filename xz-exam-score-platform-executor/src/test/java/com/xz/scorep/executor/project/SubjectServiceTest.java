package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamSubject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class SubjectServiceTest extends BaseTest {

    @Autowired
    SubjectService subjectService;

    @Test
    public void createSubjectScoreTable() throws Exception {
        subjectService.createSubjectScoreTables(PROJECT_ID);
    }

    @Test
    public void testFindSubject() throws Exception {
        ExamSubject subject = subjectService.findSubject(PROJECT2_ID, "001");
        Assert.assertNotNull(subject);
    }
}