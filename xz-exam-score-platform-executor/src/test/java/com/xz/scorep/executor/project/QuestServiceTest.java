package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import org.junit.Assert;
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

    @Test
    public void testFindQuest() throws Exception {
        ExamQuest quest1 = questService.findQuest(PROJECT_ID, "100006244-1");
        ExamQuest quest2 = questService.findQuest(PROJECT_ID, "587de1032d56028755785faa");

        Assert.assertTrue(quest1.isObjective());
        Assert.assertFalse(quest2.isObjective());

    }

    @Test
    public void testEffectiveScoreRule() throws Exception {
        String projectId = "430300-29c4d40d93bf41a5a82baffe7e714dd9";
        String questId = "58e730b52d560287557a45b8";
        ExamQuest quest = questService.findQuest(projectId, questId);
        Assert.assertNotNull(quest);
        Assert.assertEquals("A3B3C3D3AB3AC3AD3BC3BD3CD3ABC3ABD3BCD3ABCD3", quest.effectiveScoreRule());
    }
}