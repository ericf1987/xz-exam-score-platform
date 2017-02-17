package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * (description)
 * created at 2017/2/17
 *
 * @author yidin
 */
public class ScoreQueryTest extends BaseTest {

    @Autowired
    private ScoreQuery scoreQuery;

    @Test
    public void listStudentScore() throws Exception {
        List<Row> rows = scoreQuery.listStudentScore("fake_project_big",
                Range.clazz("08aa3359_4970_408a_b031_68101f46a34c"),
                Target.quest("0e43e805_2540_4c7f_8714_a11d6a316c47"));

        rows.forEach(System.out::println);
    }

}