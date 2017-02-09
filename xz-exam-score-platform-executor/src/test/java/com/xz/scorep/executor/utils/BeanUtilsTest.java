package com.xz.scorep.executor.utils;

import com.xz.scorep.executor.bean.ProjectSchool;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class BeanUtilsTest {

    @Test
    public void copyProperties() throws Exception {
        ProjectSchool school1 = new ProjectSchool("school1", "school1", null, null, null);
        ProjectSchool school2 = new ProjectSchool("school2", "school2", "430101", "430100", "430000");

        BeanUtils.fillProperties(school1, school2);
        System.out.println(ReflectionToStringBuilder.toString(school1));
    }

}