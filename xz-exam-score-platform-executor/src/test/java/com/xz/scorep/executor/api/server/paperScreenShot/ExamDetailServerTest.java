package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * @author luckylo
 */
public class ExamDetailServerTest extends BaseTest {

    @Autowired
    ExamDetailServer server;

    @Test
    public void execute() throws Exception {
        Param param = new Param();
        param.setParameter("projectId","430300-29c4d40d93bf41a5a82baffe7e714dd9");
        param.setParameter("subjectId","001");
        param.setParameter("classId","42e34943-29e9-4784-b865-585de017b56b");
        param.setParameter("studentId","0000bf2f-ecee-4745-b2bb-ee9e1abb2fd1");
        param.setParameter("schoolId","710269b3-e856-4dcb-93d4-dd274bfc0b53");
        Result execute = server.execute(param);
        System.out.println(execute.getData().toString());
    }

}