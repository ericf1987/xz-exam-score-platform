package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: luckylo
 * Date : 2017-02-27
 * 联考学校平均分全部科目
 */
@Component
public class TotalAverageSheet0 extends TotalAverageSheet {


    @Override
    protected Map<String,String> getTableHeader() {
        Map<String,String> table = new LinkedHashMap<>();
        table.put("school_name","学校名称");
        table.put("student_count","实考人数");
        table.put("max_score","最高分");
        table.put("min_score","最低分");
        table.put("average_score_zero","平均分\r\n(含0分)");
        table.put("average_score","平均分\r\n(不含0分)");
        table.put("average_range","平均分排名");
        table.put("excellent","优率");
        table.put("good","良率");
        table.put("pass","及格率");
        table.put("fail","不及格率");
        table.put("over_average","超均率");

        table.put("all_pass","全科及格率");
        table.put("all_fail","全科不及格率");

        return table;
    }

    @Override
    protected String getTargetType(SheetContext sheetContext) {
        return "project";
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return "(总分)";
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return "";
    }
}
