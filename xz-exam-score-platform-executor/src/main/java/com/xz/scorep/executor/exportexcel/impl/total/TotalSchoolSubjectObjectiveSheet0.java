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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TotalSchoolSubjectObjectiveSheet0 extends SheetGenerator {

    public static final String PROVINCE_OPTION_RATE_TEMPLATE = "select " +
            " a.*, a.`count`/b.total as province_rate from (\n" +
            "  select objective_answer as `option`, count(1) as `count`\n" +
            "  from `{{scoreTable}}` score\n" +
            "  group by objective_answer\n" +
            ") a, (\n" +
            "  select count(1) as total from student\n" +
            ") b";

    public static final String SCHOOL_OPTION_RATE_TEMPLATE = "select " +
            " a.*, a.`count`/b.total as school_rate from (\n" +
            "  select objective_answer as `option`, count(1) as `count`\n" +
            "  from `{{scoreTable}}` score, student\n" +
            "  where score.student_id=student.id and student.school_id='{{schoolId}}'\n" +
            "  group by objective_answer\n" +
            ") a, (\n" +
            "  select count(1) as total \n" +
            "  from student\n" +
            "  where student.school_id='{{schoolId}}'\n" +
            ") b";

    public static final String CLASS_OPTION_RATE_TEMPLATE = "select " +
            " a.*, a.`count`/b.total as `rate` from (\n" +
            "  select student.class_id, objective_answer as `option`, count(1) as `count`\n" +
            "  from `{{scoreTable}}` score, student\n" +
            "  where score.student_id=student.id and student.school_id='{{schoolId}}'\n" +
            "  group by student.class_id, objective_answer\n" +
            ") a, (\n" +
            "  select student.class_id, count(1) as total \n" +
            "  from student\n" +
            "  where student.school_id='{{schoolId}}'\n" +
            "  group by student.class_id\n" +
            ") b\n" +
            "where a.class_id=b.class_id";

    public static final String TABLE_KEY = "quest_option";

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
                String optionName = option.equals("*") ? "不选" : option;
                String key = objQuest.getId() + ":" + option;
                sheetContext.tablePutValue(key, "quest_no", objQuest.getQuestNo());
                sheetContext.tablePutValue(key, "option_name", optionName + "率");
            }
        }

        ////////////////////////////////////////////////////////////// 填充选择率

        DAO projectDao = daoFactory.getProjectDao(projectId);

        for (ExamQuest quest : objQuests) {
            String scoreTableName = "score_" + quest.getId();

            ////////////////////////////////////////////////////////////// 全局选择率

            String totalOptionRateSql = PROVINCE_OPTION_RATE_TEMPLATE.replace("{{scoreTable}}", scoreTableName);
            List<Row> totalOptionRates = fixKey(projectDao.query(totalOptionRateSql), quest);
            sheetContext.rowAdd(totalOptionRates);

            ////////////////////////////////////////////////////////////// 学校选择率

            String schoolOptionRateSql = SCHOOL_OPTION_RATE_TEMPLATE
                    .replace("{{scoreTable}}", scoreTableName)
                    .replace("{{schoolId}}", schoolId);
            List<Row> schoolOptionRates = fixKey(projectDao.query(schoolOptionRateSql), quest);
            sheetContext.rowAdd(schoolOptionRates);


            ////////////////////////////////////////////////////////////// 班级选择率

            String classOptionRateSql = CLASS_OPTION_RATE_TEMPLATE
                    .replace("{{scoreTable}}", scoreTableName)
                    .replace("{{schoolId}}", schoolId);
            List<Row> classOptionRates = fixClassRows(projectDao.query(classOptionRateSql), quest);
            sheetContext.rowAdd(classOptionRates);
        }

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

    private List<Row> fixClassRows(List<Row> classRateRows, ExamQuest quest) {
        Map<String, Row> rowMap = new HashMap<>();
        for (Row row : classRateRows) {
            String classId = row.getString("class_id");
            String option = row.getString("option");
            if (!rowMap.containsKey(option)) {
                Row r = new Row();
                r.put(TABLE_KEY, quest.getId() + ":" + option);
                rowMap.put(option, r);
            }

            Row mapRow = rowMap.get(option);
            String columnName = "class_rate_" + classId;
            mapRow.put(columnName, NumberUtil.toPercent(row.getDouble("rate", 0)));
        }

        return new ArrayList<>(rowMap.values());
    }

    private List<Row> fixKey(List<Row> rows, ExamQuest quest) {
        rows.forEach(row -> {
            row.put(TABLE_KEY, quest.getId() + ":" + row.getString("option"));
            row.keySet().forEach(key -> {
                if (key.contains("_rate")) {
                    row.put(key, NumberUtil.toPercent(row.getDouble(key, 0)));
                }
            });
        });
        return rows;
    }
}
