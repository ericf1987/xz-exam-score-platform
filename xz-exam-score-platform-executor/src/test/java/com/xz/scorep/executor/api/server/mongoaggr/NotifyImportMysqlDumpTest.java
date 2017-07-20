package com.xz.scorep.executor.api.server.mongoaggr;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/19.
 */
public class NotifyImportMysqlDumpTest extends BaseTest{

    @Autowired
    NotifyImportMysqlDump notifyImportMysqlDump;

    @Test
    public void testExecute() throws Exception {
        Param param = new Param().setParameter("projectId", "430100-9a564abc5f0044b4a470c2f146de50ab")
                .setParameter("filePath", "/mnt/nas/znxunzhi/dbdump/20170719/430100-9a564abc5f0044b4a470c2f146de50ab.zip");

        notifyImportMysqlDump.execute(param);
    }
}