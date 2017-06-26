package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/26.
 */
public class AbilityLevelServiceTest extends BaseTest {

    @Autowired
    AbilityLevelService abilityLevelService;

    @Test
    public void testFindProjectStudyStage() throws Exception {
        String projectId = "430200-13e01c025ac24c6497d916551b3ae7a6";
        String studyStage = abilityLevelService.findProjectStudyStage(projectId);
        System.out.println(studyStage);
    }
}