package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
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

}