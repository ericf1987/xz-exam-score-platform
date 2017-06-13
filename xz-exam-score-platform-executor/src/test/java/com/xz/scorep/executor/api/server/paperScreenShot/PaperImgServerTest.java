package com.xz.scorep.executor.api.server.paperScreenShot;

import com.alibaba.fastjson.JSON;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by fengye on 2017/5/22.
 */
public class PaperImgServerTest extends BaseTest {

    @Autowired
    PaperImgServer paperImgServer;
    private static final String PROJECT_ID = "430700-a5a39f1f86b3408d9ced3cf82eb8a1c9";
    private static final String SCHOOL_ID = "3ce843ad-87ab-45c1-a650-c142fa438159";
    private static final String CLASS_ID = "d82f9ca1-6020-4557-ad5f-335821f1b9bc";
    private static final String SUBJECT_ID = "003";
    private static final String STUDENT_ID = "04c4f670-babe-42bb-84f3-f1012c71dbad";


    @Test
    public void testExecute() throws Exception {

        Param param = new Param();
        param.setParameter("subjectId", SUBJECT_ID);
        param.setParameter("projectId", PROJECT_ID);
        param.setParameter("classId", CLASS_ID);
        param.setParameter("schoolId", SCHOOL_ID);
        param.setParameter("studentId", STUDENT_ID);
        param.setParameter("isPositive", false);
        Result execute = paperImgServer.execute(param);
        System.out.println(JSON.toJSON(execute.getData()));
    }

    @Test
    public void testCheckAndRecord() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("paper_positive", "");
        map.put("paper_reverse", "");
        paperImgServer.checkAndRecord(PROJECT_ID, SCHOOL_ID, CLASS_ID, SUBJECT_ID, STUDENT_ID, map);
    }
}