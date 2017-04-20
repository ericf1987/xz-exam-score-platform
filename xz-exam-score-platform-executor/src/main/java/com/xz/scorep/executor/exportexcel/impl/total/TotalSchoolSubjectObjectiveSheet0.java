package com.xz.scorep.executor.exportexcel.impl.total;

import com.alibaba.fastjson.JSON;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.NumberUtil;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TotalSchoolSubjectObjectiveSheet0 extends SheetGenerator {

    private static final String TABLE_KEY = "quest_option";

    private static final String QUERY_TEMPLATE = "select" +
            "   a.quest_id, a.`option`, a.range_id," +
            "   a.option_rate as {{rateAlias}}" +
            " from objective_option_rate a, quest " +
            " where " +
            "   a.quest_id=quest.id and " +
            "   quest.exam_subject=? and " +
            "   a.range_type='province' and a.option is not null";

    private static final String QUERY_TEMPLATE_SCHOOL = "select" +
            "   a.quest_id, a.`option`, a.range_id," +
            "   a.option_rate as {{rateAlias}}" +
            " from objective_option_rate a, quest " +
            " where " +
            "   a.quest_id=quest.id and " +
            "   quest.exam_subject=? and " +
            "   a.range_type='school' and" +
            "   a.range_id=? and a.option is not null";

    private static final String QUERY_TEMPLATE_CLASS = "select" +
            "   a.quest_id, a.`option`, a.range_id," +
            "   a.option_rate as {{rateAlias}}" +
            " from objective_option_rate a, quest " +
            " where " +
            "   a.quest_id=quest.id and " +
            "   quest.exam_subject=? and " +
            "   a.option is not null and" +
            "   a.range_type='class' and" +
            "   a.range_id in (select id from class where school_id=?)";

    private static final Logger LOG = LoggerFactory.getLogger(TotalSchoolSubjectObjectiveSheet0.class);

    @Autowired
    private ClassService classService;

    @Autowired
    private QuestService questService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {

        SheetTask sheetTask = sheetContext.getSheetTask();
        String projectId = sheetContext.getProjectId();

        String schoolId = sheetTask.getRange().getId();
        List<ProjectClass> projectClasses = classService.listClasses(projectId, schoolId);

        String subjectId = String.valueOf(sheetTask.getTarget().getId());
        String subjectName = sheetTask.getTarget().getName();

        ////////////////////////////////////////////////////////////// 表头

        String sheetTitle = "客观题答题统计分析表（" + subjectName + "）";

        sheetContext.headerPut(sheetTitle, 1, projectClasses.size() + 4);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("客观题题号", 1, 2);
        sheetContext.headerMove(Direction.RIGHT, Direction.RIGHT);
        sheetContext.headerPut("总体（联考）");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("本校");
        sheetContext.headerMove(Direction.RIGHT);

        for (ProjectClass projectClass : projectClasses) {
            sheetContext.headerPut(projectClass.fixedName());
            sheetContext.headerMove(Direction.RIGHT);
        }

        sheetContext.columnWidth(2, 18);

        ////////////////////////////////////////////////////////////// 列头

        sheetContext.columnSet(0, "quest_no");
        sheetContext.columnSet(1, "option_name");
        sheetContext.columnSet(2, "province_rate");
        sheetContext.columnSet(3, "school_rate");

        for (int i = 0; i < projectClasses.size(); i++) {
            ProjectClass projectClass = projectClasses.get(i);
            sheetContext.columnSet(4 + i, "class_rate_" + projectClass.getId());
        }

        ////////////////////////////////////////////////////////////// 填充题号

        sheetContext.tableSetKey(TABLE_KEY);

        List<ExamQuest> objQuests = questService.queryQuests(projectId, subjectId, true);
        List<Integer> optionCounts = new ArrayList<>();  // 最后合并单元格时用

        for (ExamQuest objQuest : objQuests) {
            List<String> options = JSONUtils.toList(JSON.parseArray(objQuest.getOptions()));
            Collections.sort(options);
            options.add("*");   // 加入不选率
            optionCounts.add(options.size());

            for (String option : options) {
                String op = option.toUpperCase();
                String optionName = option.equals("*") ? "不选" : op;
                String key = objQuest.getId() + ":" + op;
                sheetContext.tablePutValue(key, "quest_no", objQuest.getQuestNo());
                sheetContext.tablePutValue(key, "option_name", optionName + "率");
            }
        }

        ////////////////////////////////////////////////////////////// 填充选择率

        DAO projectDao = daoFactory.getProjectDao(projectId);

        String s = QUERY_TEMPLATE.replace("{{rateAlias}}", "province_rate");
        System.out.println(s);
        List<Row> provinceOptionRates = fixKey(projectDao.query(
                s, subjectId));
        sheetContext.rowAdd(provinceOptionRates);

        List<Row> schoolOptionRates = fixKey(projectDao.query(
                QUERY_TEMPLATE_SCHOOL.replace("{{rateAlias}}", "school_rate"), subjectId, schoolId));
        sheetContext.rowAdd(schoolOptionRates);

        List<Row> classOptionRates = fixKey(projectDao.query(
                QUERY_TEMPLATE_CLASS.replace("{{rateAlias}}", "class_rate"), subjectId, schoolId));
        sheetContext.rowAdd(fixClassRows(classOptionRates));

        sheetContext.fillEmptyCells(column -> column.contains("_rate"), "0%");
        sheetContext.rowSortBy("quest_no", "option_name");
        sheetContext.freeze(2, 4);
        sheetContext.saveData();

        ////////////////////////////////////////////////////////////// 合并数据部分单元格

        int rowCounter = 2;
        for (Integer optionCount : optionCounts) {
            sheetContext.getExcelWriter().mergeCells(
                    rowCounter, 0, rowCounter + optionCount - 1, 0);
            rowCounter += optionCount;
        }
    }

    private List<Row> fixClassRows(List<Row> classRateRows) {
        Map<String, Row> rowMap = new HashMap<>();
        for (Row row : classRateRows) {
            String classId = row.getString("range_id");
            String rowKey = row.getString(TABLE_KEY);

            if (!rowMap.containsKey(rowKey)) {
                Row r = new Row();
                r.put(TABLE_KEY, rowKey);
                rowMap.put(rowKey, r);
            }

            Row mapRow = rowMap.get(rowKey);
            String columnName = "class_rate_" + classId;
            mapRow.put(columnName, row.getString("class_rate"));
        }

        return new ArrayList<>(rowMap.values());
    }

    private List<Row> fixKey(List<Row> rows) {
        rows.forEach(row -> {
            row.put(TABLE_KEY, row.getString("quest_id") + ":" + row.getString("option"));
            row.keySet().forEach(key -> {
                if (key.contains("_rate")) {
                    row.put(key, NumberUtil.toPercent(row.getDouble(key, 0)));
                }
            });
        });
        return rows;
    }
}
