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
        Range range = Range.clazz("02da6ce6-91fe-4a53-9639-54ba98268c3b", "399班");
        Target target = Target.subject("002", "数学");
        String savePath = "target/399班分数排名、得分明细表（数学）.xlsx";
        subjectClassDetailReport.generate(PROJECT_ID, range, target, savePath);
    }

}