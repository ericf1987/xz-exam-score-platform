package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ProjectStudent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * (description)
 * created at 2017/2/6
 *
 * @author yidin
 */
public class StudentServiceTest extends BaseTest {

    @Autowired
    private StudentService studentService;

    @Test
    public void clearStudents() throws Exception {

    }

    @Test
    public void saveStudent() throws Exception {
        studentService.saveStudent("project1", new ProjectStudent("student1", "张三", "class1"));
    }

    @Test
    public void testGetSchoolStudentCount() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        int count = studentService.getSchoolStudentCount(projectId, schoolId);
        System.out.println(count);
    }

    @Test
    public void testCountSchoolClassStudent() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        Map<String, Integer> count = studentService.getClassStudentCount(projectId, schoolId);
        System.out.println(count);
    }
}