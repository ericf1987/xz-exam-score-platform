package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.project.ProjectService;
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
            ExcelWriter excelWriter = new ExcelWriter(stream);
            excelWriter.clearSheets();

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
