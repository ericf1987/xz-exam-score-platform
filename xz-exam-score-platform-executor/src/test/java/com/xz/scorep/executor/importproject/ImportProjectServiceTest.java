package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.StringUtil;
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
        String projectId = "430300-2ded1a538def48c08d1d82014acf55ba";
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
        String examSubjectId = "007008009";
        int size = examSubjectId.length() / 3;
        for (int i = 1; i <= size; i++) {
            String subSubjectId = examSubjectId.substring(i * size - 3, i * size);
            System.out.println(subSubjectId);
        }
    }
}