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
    private static final String PROJECT_ID = "430100-dd3013ab961946fb8a3668e5ccc475b6";
    private static final String SCHOOL_ID = "d9bdecc9-0185-4688-90d1-1aaf27e2dcfd";
    private static final String CLASS_ID = "21c44641-dabd-4e98-b2fa-eef94a9d8ffc";
    private static final String SUBJECT_ID = "001";
    private static final String STUDENT_ID = "55c0e53c-68b9-4f8b-a148-36ecd23bc758";


    @Test
    public void testExecute() throws Exception {

        Param param = new Param();
        param.setParameter("subjectId", "002");
        param.setParameter("projectId", "430200-13e01c025ac24c6497d916551b3ae7a6");
        param.setParameter("classId", "317c7b47-587c-445b-83b8-c2887e51cec1");
        param.setParameter("schoolId", "200f3928-a8bd-48c4-a2f4-322e9ffe3700");
        param.setParameter("studentId", "74a296ed-7dee-4f2d-8a8a-2ce358b39d0e");
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