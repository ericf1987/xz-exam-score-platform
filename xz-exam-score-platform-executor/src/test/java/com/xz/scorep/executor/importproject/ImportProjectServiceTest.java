package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.lang.Context;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.project.QuestService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.xz.scorep.executor.importproject.ImportProjectParameters.importSelected;
import static com.xz.scorep.executor.importproject.ImportProjectService.PROJECT_ID_KEY;

public class ImportProjectServiceTest extends BaseTest {

    @Autowired
    private ImportProjectService importProjectService;

    @Autowired
    QuestService questService;

    @Test
    public void importProject() throws Exception {
        String projectId = "431300-8fdccda1e97543b5a4d8c0bfc2f3dd4e";
        importProjectService.importProject(
                importSelected(projectId, true, true, true, true, true, true, true));
    }

    @Test
    public void testImportReportConfig() throws Exception {
        Context context = new Context();
        context.put(PROJECT_ID_KEY, 湘潭20170328联考);
        importProjectService.importReportConfig(context);
    }

    @Test
    public void testImportStudent() throws Exception {
        String examSubjectId = "007008009012";
        while (true) {
            if (examSubjectId.length() < 3) {
                break;
            }
            String id = examSubjectId.substring(0, 3);
            examSubjectId = examSubjectId.substring(3, examSubjectId.length());
            System.out.println(id);
        }
    }

    @Test
    public void testImportQuestTypes() throws Exception {
        String projectId = "430300-29c4d40d93bf41a5a82baffe7e714dd9";
        List<ExamQuest> examQuests = questService.queryQuests(projectId);
        Context context = new Context();
        context.put("questList", examQuests);
        context.put("projectId", projectId);
        importProjectService.importQuestTypes(context);
    }

    @Test
    public void testImportProject1() throws Exception {
        String projectId = "430200-13e01c025ac24c6497d916551b3ae7a6";
        List<ExamQuest> examQuests = questService.queryQuests(projectId);
        Context context = new Context();
        context.put("questList", examQuests);
        importProjectService.importPointsAndLevels(context);
    }

    @Test
    public void testImportPointsAndLevels() throws Exception {

    }

    @Test
    public void testImportQuestTypes1() throws Exception {

    }

    @Test
    public void testImportReportConfig1() throws Exception {

    }
}