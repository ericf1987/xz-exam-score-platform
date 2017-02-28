package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.NumberUtil;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelCellStyles;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TotalDistributionSheet0 extends SheetGenerator {

    public static final String TOTAL_SCHOOL_ID = "z";  // 表示总计那一行的主键 value

    @Autowired
    private DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        sheetContext.headerPut("科目", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("学校名称", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.columnWidth(1, 20);   // 学校名称字段约 22 个字符宽

        sheetContext.tableSetKey("school_id");
        sheetContext.columnSet(0, "subject_name");
        sheetContext.columnSet(1, "school_name");

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        List<Row> rows = dao.query("select " +
                "  id as school_id, " +
                "  '" + getSubjectName() + "' as subject_name, " +
                "  name as school_name " +
                "from school");

        sheetContext.rowAdd(rows);

        Row totalRow = new Row();
        totalRow.put("school_id", TOTAL_SCHOOL_ID);
        totalRow.put("subject_name", getSubjectName());
        totalRow.put("school_name", "总计");
        sheetContext.rowAdd(totalRow);
        sheetContext.rowStyle(TOTAL_SCHOOL_ID, ExcelCellStyles.Green.name());

        ////////////////////////////////////////////////////////////// 统计各学校的分数段人数

        List<Row> schoolSegments = dao.query("select \n" +
                "  range_id as school_id,\n" +
                "  score_min,score_max,student_count \n" +
                "from segments\n" +
                "where \n" +
                "  range_type='school' and target_type='" + getTargetType() + "'");

        int total = schoolSegments.stream().mapToInt(row -> row.getInteger("student_count", 0)).sum();
        List<String> colSuffixes = new ArrayList<>();  // 整理分数段然后用于填充表头

        // 将查询结果竖表转横表，填入 table 中
        for (Row row : schoolSegments) {
            fillSegmentTable(sheetContext, total, colSuffixes, row, row.getString("school_id"));
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

        List<Row> totalSegments = dao.query("select " +
                "  score_min,score_max,student_count " +
                "from segments " +
                "where range_type='province' and target_type='project'");

        total = totalSegments.stream().mapToInt(row -> row.getInteger("student_count", 0)).sum();

        for (Row row : totalSegments) {
            fillSegmentTable(sheetContext, total, colSuffixes, row, TOTAL_SCHOOL_ID);
        }

        ////////////////////////////////////////////////////////////// 写入 Sheet

        sheetContext.saveData();                      // 保存到 ExcelWriter
        sheetContext.freeze(2, 2);  // 在适当的位置冻结窗口
    }

    private String getTargetType() {
        return "project";
    }

    private void fillSegmentTable(SheetContext sheetContext, int total, List<String> colSuffixes, Row row, String key) {

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

    //////////////////////////////////////////////////////////////

    protected String getSubjectName() {
        return "总分";
    }
}
