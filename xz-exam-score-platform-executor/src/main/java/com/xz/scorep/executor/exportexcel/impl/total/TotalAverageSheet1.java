package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-02-27
 */
@Component
public class TotalAverageSheet1 extends TotalAverageSheet {


    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {

    }

    @Override
    protected String[] getTableHeader() {
        return new String[] {
                "学校名称", "实考人数", "最高分",
                "最低分", "平均分", "平均分排名",
                "优率", "良率", "及格率", "不及格率",
                "超均率"};
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return sheetContext.getProperties().get("subjectName");
    }
}
