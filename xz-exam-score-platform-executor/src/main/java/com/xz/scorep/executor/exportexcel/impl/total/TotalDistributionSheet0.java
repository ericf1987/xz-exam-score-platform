package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.NumberUtil;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.xz.scorep.executor.exportexcel.SheetContext.STYLE_CENTERED;

@Component
public class TotalDistributionSheet0 extends SheetGenerator {

    @Autowired
    private DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        sheetContext.headerPut("科目", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("学校名称", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.columnWidth(1, 20);   // 学校名称字段约 20 个字符宽

        sheetContext.tableSetKey("school_id");
        sheetContext.columnSet(0, "subject_name");
        sheetContext.columnSet(1, "school_name");

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        List<Row> rows = dao.query("select " +
                "  id as school_id, " +
                "  '总分' as subject_name, " +
                "  name as school_name " +
                "from school");

        sheetContext.rowAdd(rows);

        Row totalRow = new Row();
        totalRow.put("school_id", "z");
        totalRow.put("subject_name", "总分");
        totalRow.put("school_name", "总计");
        sheetContext.rowAdd(totalRow);

        List<Row> schoolSegments = dao.query("select \n" +
                "  range_id as school_id,\n" +
                "  score_min,score_max,student_count \n" +
                "from segments\n" +
                "where \n" +
                "  range_type='school' and target_type='project'");

        int total = schoolSegments.stream().mapToInt(row -> row.getInteger("student_count", 0)).sum();
        List<String> colSuffixes = new ArrayList<>();

        for (Row row : schoolSegments) {
            String colNameSuffix = row.getString("score_max") + "-" + row.getString("score_min");
            if (!colSuffixes.contains(colNameSuffix)) {
                colSuffixes.add(colNameSuffix);
            }

            int count = row.getInteger("student_count", 0);
            double rate = NumberUtil.scale(100.0 * count / total, 2);
            String rateStr = String.format("%.02f%%", rate);
            sheetContext.tablePutValue(row.getString("school_id"), "count_" + colNameSuffix, count);
            sheetContext.tablePutValue(row.getString("school_id"), "rate_" + colNameSuffix, rateStr);
        }

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
            sheetContext.columnStyle(3 + i * 2, STYLE_CENTERED);    // 设置列样式
        }

        sheetContext.saveData();                      // 保存到 ExcelWriter
        sheetContext.freeze(2, 2);  // 在适当的位置冻结窗口
    }
}
