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

/**
 * (description)
 * created at 16/05/30
 *
 * @author yiding_he
 */
public abstract class ReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

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

            for (SheetTask sheetTask : sheetTasks) {
                excelWriter.openOrCreateSheet(sheetTask.getTitle());
                SheetGenerator sheetGenerator = sheetManager.getSheetGenerator(sheetTask.getGeneratorClass());
                if (sheetGenerator != null) {
                    sheetGenerator.generate(project, excelWriter, sheetTask);
                }
            }

            excelWriter.save(savePath);
        } catch (Throwable e) {
            LOG.error("生成报表失败", e);
        }

        LOG.info("生成报表 " + this.getClass() + " 结束。");
    }

    private ExcelWriter createExcelWriter(InputStream stream) {
        ExcelWriter excelWriter = new ExcelWriter(stream);
        excelWriter.clearSheets();

        createHeaderStyle(excelWriter);
        createDataCenteredStyle(excelWriter);
        createGreenRowStyle(excelWriter);

        return excelWriter;
    }

    private void createGreenRowStyle(ExcelWriter excelWriter) {
        CellStyle cellStyle = excelWriter.createCellStyle(SheetContext.STYLE_GREEN);
        cellStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
    }

    private void createDataCenteredStyle(ExcelWriter excelWriter) {
        CellStyle style = excelWriter.createCellStyle(SheetContext.STYLE_CENTERED);
        style.setAlignment(HorizontalAlignment.CENTER);
    }

    private void createHeaderStyle(ExcelWriter excelWriter) {
        CellStyle headerStyle = excelWriter.createCellStyle(SheetContext.STYLE_HEADER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = excelWriter.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
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
