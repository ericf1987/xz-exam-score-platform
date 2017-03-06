package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.NumberUtil;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelCellStyles;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 填充各种 Range 和 Target 的分数分布表
 */
@Component
public class ScoreDistributionSheet extends SheetGenerator {

    private static final String ITEM_ID = "item_id";

    @Autowired
    protected DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        String projectId = sheetContext.getProjectId();
        SheetTask sheetTask = sheetContext.getSheetTask();
        DAO projectDao = this.getProjectDao(projectId);

        generateSheet0(sheetContext, projectDao, sheetTask.getRange(), sheetTask.getTarget());
    }

    protected DAO getProjectDao(String projectId) {
        return daoFactory.getProjectDao(projectId);
    }

    private static void generateSheet0(
            SheetContext sheetContext, DAO projectDao, Range range, Target target) {

        sheetContext.headerPut("科目", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut(range.match(Range.PROVINCE) ? "学校名称" : "班级", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.columnWidth(1, 20);   // 学校名称字段约 20 个字符宽

        sheetContext.tableSetKey(ITEM_ID);
        sheetContext.columnSet(0, "subject_name");
        sheetContext.columnSet(1, "item_name");

        // item 列表
        String tableName = range.match(Range.PROVINCE) ? "school" : "class";
        String subjectName = target.match(Target.PROJECT)? "总分": target.getName();
        List<Row> itemRows = projectDao.query(SQL
                .Select("id as item_id",
                        "name as item_name",
                        "'" + subjectName + "' as subject_name")
                .From(tableName)
                .Where(range.match(Range.SCHOOL), "school_id=?", range.getId())
        );

        sheetContext.rowAdd(itemRows);

        ////////////////////////////////////////////////////////////// 统计各学校的分数段人数

        String itemRangeType = range.match(Range.PROVINCE) ? "school" : "class";
        List<Row> itemSegments = projectDao.query(SQL
                .Select("range_id as item_id", "score_min", "score_max", "student_count")
                .From("segments")
                .Where("range_type=?", itemRangeType)
                .And("target_type=?", target.getType())
                .And("target_id=?", target.getId())
                .And(range.match(Range.SCHOOL), "range_id in (select id from class where school_id=?)", range.getId())
        );

        int total = itemSegments.stream().mapToInt(row -> row.getInteger("student_count", 0)).sum();
        List<String> colSuffixes = new ArrayList<>();  // 整理分数段然后用于填充表头

        // 将查询结果竖表转横表，填入 table 中
        for (Row row : itemSegments) {
            fillSegmentTable(sheetContext, total, colSuffixes, row);
        }

        // 填充分数段的表头
        colSuffixes.sort(new NaturalOrderComparator().reversed());
        for (int i = 0; i < colSuffixes.size(); i++) {
            String colSuffix = colSuffixes.get(i);
            sheetContext.headerPut(colSuffix, 1, 2);
            sheetContext.headerMove(Direction.DOWN);
            sheetContext.headerPut("人数");
            sheetContext.headerMove(Direction.RIGHT);
            sheetContext.headerPut("占比");
            sheetContext.headerMove(Direction.RIGHT, Direction.UP);
            sheetContext.columnSet(2 + i * 2, "count_" + colSuffix);
            sheetContext.columnSet(3 + i * 2, "rate_" + colSuffix);
        }

        ////////////////////////////////////////////////////////////// 统计所有整体的分数段人数

        Row totalRow = new Row();
        totalRow.put("item_id", "z");
        totalRow.put("item_name", "总计");
        totalRow.put("subject_name", subjectName);
        sheetContext.rowAdd(totalRow);
        sheetContext.rowStyle("z", ExcelCellStyles.Green.name());

        List<Row> totalSegments = projectDao.query(SQL
                .Select("'z' as item_id", "score_min", "score_max", "student_count")
                .From("segments")
                .Where("range_type=?", range.getType())
                .And(range.match(Range.SCHOOL), "range_id=?", range.getId())
                .And("target_type=?", target.getType())
                .And("target_id=?", target.getId())
        );

        total = totalSegments.stream().mapToInt(row -> row.getInteger("student_count", 0)).sum();

        for (Row row : totalSegments) {
            fillSegmentTable(sheetContext, total, colSuffixes, row);
        }

        ////////////////////////////////////////////////////////////// 写入 Sheet

        sheetContext.fillEmptyCells(col -> col.startsWith("count_"), 0);
        sheetContext.fillEmptyCells(col -> col.startsWith("rate_"), "0%");
        sheetContext.saveData();                      // 保存到 ExcelWriter
        sheetContext.freeze(2, 2);  // 在适当的位置冻结窗口
    }

    private static void fillSegmentTable(SheetContext sheetContext, int total, List<String> colSuffixes, Row row) {

        String key = row.getString(ITEM_ID);
        String colNameSuffix = row.getDoubleObject("score_max").intValue()
                + "-" + row.getDoubleObject("score_min").intValue();

        if (!colSuffixes.contains(colNameSuffix)) {
            colSuffixes.add(colNameSuffix);
        }

        int count = row.getInteger("student_count", 0);
        double rate = NumberUtil.scale(100.0 * count / total, 2);
        String rateStr = String.format("%.02f%%", rate);
        sheetContext.tablePutValue(key, "count_" + colNameSuffix, count);
        sheetContext.tablePutValue(key, "rate_" + colNameSuffix, rateStr);
    }

}
