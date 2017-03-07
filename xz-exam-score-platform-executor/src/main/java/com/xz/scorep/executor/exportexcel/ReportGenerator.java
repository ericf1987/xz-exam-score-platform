package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.project.ProjectService;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;

import static org.apache.poi.ss.usermodel.IndexedColors.BLACK;

/**
 * (description)
 * created at 16/05/30
 *
 * @author yiding_he
 */
public abstract class ReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private static final String DEFAULT_FONT = "defaultFont";

    @Autowired
    private SheetManager sheetManager;

    @Autowired
    private ProjectService projectService;

    /**
     * 生成并保存报表文件
     *
     * @param projectId 项目ID
     * @param range     相关的范围
     * @param target    相关的目标
     * @param savePath  保存路径
     */
    public void generate(String projectId, Range range, Target target, String savePath) {
        try {
            ExamProject project = projectService.findProject(projectId);
            List<SheetTask> sheetTasks = getSheetTasks(projectId, range, target);
            InputStream stream = getClass().getResourceAsStream("report/templates/default.xlsx");
            ExcelWriter excelWriter = createExcelWriter(stream);
            String className = this.getClass().getSimpleName();

            for (SheetTask sheetTask : sheetTasks) {
                sheetTask.setReportName(className);
                excelWriter.openOrCreateSheet(sheetTask.getTitle());
                initSheet(excelWriter.getCurrentSheet());  // 当前的 sheet 一定是刚刚创建或打开的那个
                SheetGenerator sheetGenerator = sheetManager.getSheetGenerator(sheetTask.getGeneratorClass());
                if (sheetGenerator != null) {
                    sheetGenerator.generate(project, excelWriter, sheetTask);
                }
            }

            excelWriter.save(savePath);
        } catch (Throwable e) {
            LOG.error("生成报表失败", e);
        }
    }

    private void initSheet(Sheet sheet) {
        sheet.setDefaultRowHeightInPoints(20);
    }

    private ExcelWriter createExcelWriter(InputStream stream) {
        ExcelWriter excelWriter = new ExcelWriter(stream);
        excelWriter.clearSheets();

        createFontWithName(DEFAULT_FONT, excelWriter, 11, false, BLACK);

        createDefaultStyle (excelWriter);
        createHeaderStyle  (excelWriter);
        createGreenRowStyle(excelWriter);

        return excelWriter;
    }

    private Font createFontWithName(String name, ExcelWriter excelWriter, int fontSize, boolean bold, IndexedColors color) {
        return excelWriter.createFont(name, "宋体", fontSize, bold, color);
    }

    private void resetStyle(ExcelWriter excelWriter, CellStyle cellStyle) {
        cellStyle.setFont(excelWriter.getFont(DEFAULT_FONT));
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private void createDefaultStyle(ExcelWriter excelWriter) {
        Workbook workbook = excelWriter.getWorkbook();
        CellStyle cellStyle = workbook.getCellStyleAt(0);
        resetStyle(excelWriter, cellStyle);
    }

    private void createGreenRowStyle(ExcelWriter excelWriter) {
        CellStyle cellStyle = excelWriter.createCellStyle(ExcelCellStyles.Green.name());
        resetStyle(excelWriter, cellStyle);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void createHeaderStyle(ExcelWriter excelWriter) {
        CellStyle cellStyle = excelWriter.createCellStyle(ExcelCellStyles.Header.name());
        resetStyle(excelWriter, cellStyle);
        cellStyle.setFont(createFontWithName("header", excelWriter, 11, true, BLACK));
    }

    /**
     * 规划这个 Excel 文件有多少个 Sheet，为每个 Sheet 创建一个 SheetTask 对象
     *
     * @param projectId 项目ID
     * @param range     相关的范围
     * @param target    相关的目标
     *
     * @return SheetTask 列表
     */
    protected abstract List<SheetTask> getSheetTasks(String projectId, Range range, Target target);
}
