package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * (description)
 * created at 2017/2/6
 *
 * @author yidin
 */
public class QuestServiceTest extends BaseTest {

    @Autowired
    private QuestService questService;

    @Test
    public void clearQuests() throws Exception {

    }

    @Test
    public void saveQuest() throws Exception {
        ExamQuest examQuest = new ExamQuest("quest1", "001", true, "1", 4.0);
        questService.saveQuest("project1", examQuest);
    }

    @Test
    public void testQueryQuest() throws Exception {
        List<ExamQuest> examQuests;
        examQuests = questService.queryQuests(PROJECT_ID, "001", true);
        examQuests = questService.queryQuests(PROJECT_ID, "001", true);
        examQuests = questService.queryQuests(PROJECT_ID, "001", true);
        examQuests = questService.queryQuests(PROJECT_ID, "001", true);
    }
}