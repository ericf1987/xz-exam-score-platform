package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.lang.Context;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.xz.scorep.executor.importproject.ImportProjectParameters.importSelected;
import static com.xz.scorep.executor.importproject.ImportProjectService.PROJECT_ID_KEY;

public class ImportProjectServiceTest extends BaseTest {

    @Autowired
    private ImportProjectService importProjectService;

    @Test
    public void importProject() throws Exception {
        String projectId = "431300-8fdccda1e97543b5a4d8c0bfc2f3dd4e";
        importProjectService.importProject(
                importSelected(projectId, true, true, true, true, true, true));
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
}