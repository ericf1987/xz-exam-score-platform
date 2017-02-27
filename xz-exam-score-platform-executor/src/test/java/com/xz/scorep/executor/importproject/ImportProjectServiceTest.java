package com.xz.scorep.executor.importproject;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.xz.scorep.executor.importproject.ImportProjectParameters.importSelected;

public class ImportProjectServiceTest extends BaseTest {

    @Autowired
    private ImportProjectService importProjectService;

    @Test
    public void importProject() throws Exception {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        importProjectService.importProject(importSelected(projectId, false, false, false, false, true, true));
    }

}