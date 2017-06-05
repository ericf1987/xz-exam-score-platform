package com.xz.scorep.executor.api.server.paperScreenShot;

import com.alibaba.fastjson.JSON;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/5/22.
 */
public class PaperImgServerTest extends BaseTest {

    @Autowired
    PaperImgServer paperImgServer;

    @Test
    public void testExecute() throws Exception {

        Param param = new Param();
        param.setParameter("subjectId", "001");
        param.setParameter("projectId", "430700-a5a39f1f86b3408d9ced3cf82eb8a1c9");
        param.setParameter("classId", "d82f9ca1-6020-4557-ad5f-335821f1b9bc");
        param.setParameter("schoolId", "3ce843ad-87ab-45c1-a650-c142fa438159");
        param.setParameter("studentId", "04c4f670-babe-42bb-84f3-f1012c71dbad");
        param.setParameter("isPositive", false);
        Result execute = paperImgServer.execute(param);
        System.out.println(JSON.toJSON(execute.getData()));
    }
}