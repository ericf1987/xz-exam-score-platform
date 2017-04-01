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
        String projectId = "430100-172622b784994963baa740c6a3a1f532";
        importProjectService.importProject(
                importSelected(projectId, false, false, false, false, true, true));
    }

    @Test
    public void testImportReportConfig() throws Exception {
        Context context = new Context();
        context.put(PROJECT_ID_KEY, 湘潭20170328联考);
        importProjectService.importReportConfig(context);
    }

    @Test
    public void testImportStudent() throws Exception {

    }
}