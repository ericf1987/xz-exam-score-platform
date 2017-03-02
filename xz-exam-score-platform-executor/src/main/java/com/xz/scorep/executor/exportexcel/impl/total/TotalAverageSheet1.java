package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: luckylo
 * Date : 2017-02-27
 */
@Component
public class TotalAverageSheet1 extends TotalAverageSheet {


    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        Target target = sheetContext.getSheetTask().getTarget();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        sheetContext.getProperties().put("subjectId", subjectId);
        sheetContext.getProperties().put("subjectName", subjectName);

        generateEachSheet(sheetContext);
    }

    @Override
    protected Map<String,String> getTableHeader() {
        Map<String,String> table = new LinkedHashMap<>();
        table.put("school_name","学校名称");
        table.put("student_count","实考人数");
        table.put("max_score","最高分");
        table.put("min_score","最低分");
        table.put("average_score","平均分");
        table.put("average_range","平均分排名");
        table.put("excellent","优率");
        table.put("good","良率");
        table.put("pass","及格率");
        table.put("fail","不及格率");
        table.put("over_average","超均率");
        return table;
    }


    @Override
    protected String getTargetType(SheetContext sheetContext) {
        return "subject";
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return "("+sheetContext.getProperties().get("subjectName")+")";
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return sheetContext.getProperties().get("subjectId");
    }
}
