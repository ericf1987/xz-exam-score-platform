package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TotalSchoolSubjectObjectiveSheet1 extends SheetGenerator {

    public static final String PROVINCE_SCHOOL_SCORE = "select " +
            "  '{{questNo}}' as quest_no, a.*, b.*, c.* \n" +
            "from (\n" +
            "  select full_score \n" +
            "  from quest \n" +
            "  where id='{{questId}}'\n" +
            ") a, (\n" +
            "  select AVG(score.score) as province_score \n" +
            "  from `score_{{questId}}` score\n" +
            ") b, (\n" +
            "  select AVG(score.score) as school_score \n" +
            "  from `score_{{questId}}` score, student\n" +
            "  where score.student_id=student.id \n" +
            "    and student.school_id='{{schoolId}}'\n" +
            ") c";

    public static final String CLASS_QUEST_SCORE = "select " +
            "  '{{questNo}}' as quest_no, class_id, AVG(score.score) as class_score \n" +
            "from `score_{{questId}}` score, student\n" +
            "where score.student_id=student.id \n" +
            "  and student.school_id='{{schoolId}}'\n" +
            "group by student.class_id  ";

    public static final String TABLE_KEY = "quest_no";

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

        setupColumns(sheetContext, projectClasses, subjectName);

        ////////////////////////////////////////////////////////////// 表格内容

        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId, false);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        for (ExamQuest examQuest : examQuests) {
            String questId = examQuest.getId();
            String questNo = examQuest.getQuestNo();

            sheetContext.tablePutValue(questNo, TABLE_KEY, questNo);

            ////////////////////////////////////////////////////////////// 查询整体和学校

            String sql = PROVINCE_SCHOOL_SCORE
                    .replace("{{questNo}}", questNo).replace("{{questId}}", questId).replace("{{schoolId}}", schoolId);

            List<Row> rows = projectDao.query(sql);
            rows.forEach(row -> {
                fixRow(examQuest, row, "province_score", "province_score", "province_rate");
                fixRow(examQuest, row, "school_score", "school_score", "school_rate");
            });
            sheetContext.rowAdd(rows);

            ////////////////////////////////////////////////////////////// 查询班级

            String classSQl = CLASS_QUEST_SCORE
                    .replace("{{questNo}}", questNo).replace("{{questId}}", questId).replace("{{schoolId}}", schoolId);

            List<Row> classRows = projectDao.query(classSQl);
            Row turnedRow = new Row();      // 竖表转横表
            turnedRow.put(TABLE_KEY, questNo);

            classRows.forEach(row -> {
                String classId = row.getString("class_id");
                double originalScore = row.getDouble("class_score", 0);
                turnedRow.put("class_score_" + classId, NumberUtil.scale(originalScore, 3));
                turnedRow.put("class_rate_" + classId, NumberUtil.toPercent(originalScore / examQuest.getFullScore()));
            });

            sheetContext.rowAdd(turnedRow);
        }

        sheetContext.rowSortBy(TABLE_KEY);
        sheetContext.saveData();
        sheetContext.freeze(3, 5);
    }

    private void fixRow(ExamQuest examQuest, Row row, String readScoreColumn, String saveScoreColumn, String rateColumn) {
        double originalScore = row.getDouble(readScoreColumn, 0);
        row.put(saveScoreColumn, NumberUtil.scale(originalScore, 3));
        row.put(rateColumn, NumberUtil.toPercent(originalScore / examQuest.getFullScore()));
    }

    private void setupColumns(SheetContext sheetContext, List<ProjectClass> projectClasses, String subjectName) {
        String sheetTitle = "主观题大题统计分析表（" + subjectName + "）";
        AtomicInteger colIndex = new AtomicInteger(-1);

        sheetContext.tableSetKey(TABLE_KEY);
        sheetContext.columnWidth(0, 18);

        sheetContext.headerPut(sheetTitle, 1, projectClasses.size() * 2 + 5);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("主观题题号", 2, 1);
        sheetContext.columnSet(colIndex.incrementAndGet(), TABLE_KEY);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("总体（联考）", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("分数");
        sheetContext.columnSet(colIndex.incrementAndGet(), "province_score");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("得分率");
        sheetContext.columnSet(colIndex.incrementAndGet(), "province_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);
        sheetContext.headerPut("本校", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("分数");
        sheetContext.columnSet(colIndex.incrementAndGet(), "school_score");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("得分率");
        sheetContext.columnSet(colIndex.incrementAndGet(), "school_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);

        for (ProjectClass projectClass : projectClasses) {
            sheetContext.headerPut(projectClass.fixedName(), 1, 2);
            sheetContext.headerMove(Direction.DOWN);
            sheetContext.headerPut("分数");
            sheetContext.columnSet(colIndex.incrementAndGet(), "class_score_" + projectClass.getId());
            sheetContext.headerMove(Direction.RIGHT);
            sheetContext.headerPut("得分率");
            sheetContext.columnSet(colIndex.incrementAndGet(), "class_rate_" + projectClass.getId());
            sheetContext.headerMove(Direction.RIGHT, Direction.UP);
        }
    }
}
