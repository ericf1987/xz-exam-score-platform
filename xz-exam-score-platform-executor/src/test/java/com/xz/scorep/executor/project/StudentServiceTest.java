package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ProjectStudent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

}