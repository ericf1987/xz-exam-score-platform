package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class SubjectClassDetailReportTest extends BaseTest {

    @Autowired
    private SubjectClassDetailReport subjectClassDetailReport;

    @Test
    public void getSheetTasks() throws Exception {
        Range range = Range.clazz("42f66486-e24c-4ea7-bf1a-28bbd7313c72", "399班");
        Target target = Target.subject("001", "语文");
        String savePath = "target/399班分数排名、得分明细表（数学）.xlsx";
        subjectClassDetailReport.generate("430100-5d2142085fc747c9b5b230203bbfd402", range, target, savePath);
    }

}