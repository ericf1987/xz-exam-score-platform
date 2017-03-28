package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * (description)
 * created at 2017/3/7
 *
 * @author yidin
 */
public class AllPassOrFailAggregatorTest extends BaseTest {

    @Autowired
    AllPassOrFailAggregator allPassOrFailAggregator;

    @Test
    public void testAggregate() throws Exception {
        allPassOrFailAggregator.aggregate(new AggregateParameter(PROJECT3_ID));
    }

    @Test
    public void test() {
        String SQL = "select student.id as student_id,student.class_id,student.school_id " +
                " {{cols} from student {{tables}} where {{sub}}";
        List<String> list = Arrays.asList("001",
                "002", "003",
                "004", "005",
                "008", "013");

        StringBuffer cols = new StringBuffer();
        StringBuffer tables = new StringBuffer();
        StringBuffer sub = new StringBuffer();
        for (String str : list) {
            String id = str;
            cols.append(",score_subject_" + id + ".score as score_" + id);
            tables.append(",score_subject_" + id);
            sub.append(" student.id = score_subject_" + id + ".student_id and");
        }
        String str = sub.substring(0, sub.lastIndexOf("and")).toString();

        String sql = SQL
                .replace("{{cols}", cols.toString())
                .replace("{{tables}}", tables.toString())
                .replace("{{sub}}", str);
        System.out.println();
    }


}