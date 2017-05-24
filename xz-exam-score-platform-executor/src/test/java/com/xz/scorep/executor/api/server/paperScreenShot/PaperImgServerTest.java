package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/5/22.
 */
public class PaperImgServerTest extends BaseTest {

    @Autowired
    PaperImgServer paperImgServer;

    @Test
    public void testExecute() throws Exception {

        Param param = new Param().setParameter("projectId", "123")
                .setParameter("schoolId", "456");
        Result result = paperImgServer.execute(param);
        Map<String, Object> data = result.getData();
    }
}