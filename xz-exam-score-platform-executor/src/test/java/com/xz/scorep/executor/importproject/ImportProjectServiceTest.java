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
        String projectId = "430100-172622b784994963baa740c6a3a1f532";
        importProjectService.importProject(
                importSelected(projectId, false, false, false, false, true, true));
    }

}