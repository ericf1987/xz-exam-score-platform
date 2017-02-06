package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ProjectSchool;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class SchoolServiceTest extends BaseTest {

    @Autowired
    private SchoolService schoolService;

    @Test
    public void saveSchool() throws Exception {
        ProjectSchool school = new ProjectSchool();
        school.setId("SCHOOL1");
        school.setName("新东方学校");
        school.setArea("430101");
        school.setCity("430100");
        school.setProvince("430000");
        schoolService.saveSchool("project1", school);
    }

}