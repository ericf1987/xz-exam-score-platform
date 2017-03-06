package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
public class TotalClassDetailReportTest extends BaseTest {
    @Autowired
    TotalClassDetailReport report;

    @Test
    public void testGenerateReport() throws Exception {
        Range range = Range.clazz("02da6ce6-91fe-4a53-9639-54ba98268c3b", "399班");
        Target target = Target.subject("000", "全科");
        String savePath = "399班分数排名、得分明细表（全科）.xlsx";
        report.generate(PROJECT_ID, range, target, savePath);
    }
}