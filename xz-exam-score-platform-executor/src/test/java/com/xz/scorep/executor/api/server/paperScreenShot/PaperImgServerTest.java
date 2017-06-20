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
    private static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";
    private static final String SCHOOL_ID = "710269b3-e856-4dcb-93d4-dd274bfc0b53";
    private static final String CLASS_ID = "42e34943-29e9-4784-b865-585de017b56b";
    private static final String SUBJECT_ID = "001";
    private static final String STUDENT_ID = "e0ca103f-7c32-4729-8d62-3d7ad9983420";


    @Test
    public void testExecute() throws Exception {

        Param param = new Param();
        param.setParameter("subjectId", SUBJECT_ID);
        param.setParameter("projectId", PROJECT_ID);
        param.setParameter("classId", CLASS_ID);
        param.setParameter("schoolId", SCHOOL_ID);
        param.setParameter("studentId", STUDENT_ID);
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