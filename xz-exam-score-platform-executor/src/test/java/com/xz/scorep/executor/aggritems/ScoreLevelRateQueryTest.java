package com.xz.scorep.executor.aggritems;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ScoreLevelRate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class ScoreLevelRateQueryTest extends BaseTest {

    @Autowired
    private ScoreLevelRateQuery scoreLevelRateQuery;

    @Test
    public void testGetSchoolProjectSLR() throws Exception {
        List<ScoreLevelRate> list = scoreLevelRateQuery.getSchoolProjectSLR(PROJECT_ID, SCHOOL_ID);
        list.forEach(System.out::println);
    }

    @Test
    public void testGetClassProjectSLRs() throws Exception {
        Map<String, List<ScoreLevelRate>> classProjectSLRs = scoreLevelRateQuery.getClassProjectSLRs(PROJECT_ID, SCHOOL_ID);
        classProjectSLRs.entrySet().forEach(entry -> {
            System.out.println(entry.getKey());
            entry.getValue().forEach(value -> System.out.println("    " + value));
        });
    }
}