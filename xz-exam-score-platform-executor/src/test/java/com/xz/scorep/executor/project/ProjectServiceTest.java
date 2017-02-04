package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.beans.exam.ExamProject;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.utils.UuidUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class ProjectServiceTest extends BaseTest {

    @Autowired
    private ProjectService projectService;

    @Test
    public void saveProject() throws Exception {
        ExamProject project = new ExamProject();
        project.setId(UuidUtils.uuid());
        project.setName("TEST PROJECT");
        project.setGrade(12);
        projectService.saveProject(project);
    }

    @Test
    public void findProject() throws Exception {
        String projectId = "93fbcfd9_3499_42eb_b9f3_c3299cbcb049";
        ExamProject project = projectService.findProject(projectId);
        assertEquals("TEST PROJECT", project.getName());
        assertEquals(projectId, project.getId());
        assertEquals(12, project.getGrade());
    }

}