package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-07-11.
 */
public class TotalScoreRankReportTest extends BaseTest {

    @Autowired
    TotalScoreRankReport rankReport;

    @Autowired
    ReportCacheInitializer cacheInitializer;

    @Test
    public void generate() throws Exception {
        String savePath = "./target/联考排名得分明细表.xlsx";
        String  projectId = "430100-59f2afde35a44b92ae3d2c4c10bd4075";
        cacheInitializer.initReportCache(projectId);
//        Range range = Range.province("430100-dd3013ab961946fb8a3668e5ccc475b6");
        rankReport.generate(projectId,Range.PROVINCE_RANGE,null,savePath);
    }

}