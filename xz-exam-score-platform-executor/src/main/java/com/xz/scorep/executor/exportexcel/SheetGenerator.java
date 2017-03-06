package com.xz.scorep.executor.exportexcel;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.ajiaedu.common.lang.Ranker;
import com.xz.scorep.executor.bean.ExamProject;

import java.util.List;

/**
 * 生成报表文件中的单个 Sheet
 *
 * @author yiding_he
 */
public abstract class SheetGenerator {

    /**
     * 生成 sheet
     *
     * @param project     项目
     * @param excelWriter Excel 写入对象
     * @param sheetTask   任务对象（包含 range 和 target）
     */
    public void generate(ExamProject project, ExcelWriter excelWriter, SheetTask sheetTask) throws Exception {
        SheetContext sheetContext = new SheetContext();
        sheetContext.setProject(project);
        sheetContext.setExcelWriter(excelWriter);
        sheetContext.setSheetTask(sheetTask);

        long start = System.currentTimeMillis();
        generateSheet(sheetContext);
        long duration = System.currentTimeMillis() - start;
    }

    protected abstract void generateSheet(SheetContext sheetContext) throws Exception;

    // 1. 从 scoreColumnName 属性获取分数，
    // 2. 对分数进行排名，
    // 3. 将排名写入 rankColumnName 属性
    protected void writeRanks(List<Row> rows, String scoreColumnName, String rankColumnName) {

        Ranker<String> ranker = new Ranker<>();

        rows.forEach(row -> {
            String studentId = row.getString("student_id");
            double score = row.getDouble(scoreColumnName, 0);
            ranker.put(studentId, score);
        });

        rows.forEach(row -> {
            String studentId = row.getString("student_id");
            int rank = ranker.getRank(studentId, false);
            row.put(rankColumnName, rank + 1);  // ranker 输出的名次，0 表示第一名
        });
    }
}
