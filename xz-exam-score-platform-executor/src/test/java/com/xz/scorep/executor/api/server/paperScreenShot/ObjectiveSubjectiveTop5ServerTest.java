package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luckylo
 */
public class ObjectiveSubjectiveTop5ServerTest extends BaseTest {

    @Autowired
    ObjectiveSubjectiveTop5Server server;

    @Test
    public void execute() throws Exception {
        Param param = new Param();
        param.setParameter("projectId", "430300-29c4d40d93bf41a5a82baffe7e714dd9");
        param.setParameter("subjectId", "002");
        param.setParameter("classId", "42e34943-29e9-4784-b865-585de017b56b");
        param.setParameter("studentId", "0000bf2f-ecee-4745-b2bb-ee9e1abb2fd1");
        Result execute = server.execute(param);
        System.out.println(execute.getData().toString());
    }


    @Test
    public void test1() {
        Map<String, Integer> map = new HashMap<>();
        map.put("123", 2);
        map.put("225", 1);
        map.put("345", 4);
        map.put("4751", 5);
        map.put("4561", 9);
        map.put("4564641", 6);
        map.put("14654", 11);
        map.put("1464", 10);
        map.put("45461", 8);
        map.put("14645", 3);

        List<Map.Entry<String, Integer>> collect1 = map.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue())
                .limit(5)
                .collect(Collectors.toList());


        collect1.forEach(entry -> System.out.println(entry.getValue()));
    }
}