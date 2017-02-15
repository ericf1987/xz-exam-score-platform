package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.bean.Range;
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

    static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    @Autowired
    SheetManager sheetManager;

    /**
     * 生成并保存报表文件
     *
     * @param projectId 项目ID
     * @param range     相关的范围
     * @param savePath  保存路径
     */
    public void generate(String projectId, Range range, String savePath) {
        try {
            List<SheetTask> sheetTasks = getSheetTasks(projectId, range);
            InputStream stream = getClass().getResourceAsStream("report/templates/default.xlsx");
            ExcelWriter excelWriter = new ExcelWriter(stream);
            excelWriter.clearSheets();

            for (SheetTask sheetTask : sheetTasks) {
                sheetTask.setRange(range);
                excelWriter.openOrCreateSheet(sheetTask.getTitle());
                SheetGenerator sheetGenerator = sheetManager.getSheetGenerator(sheetTask.getGeneratorClass());
                if (sheetGenerator != null) {
                    sheetGenerator.generate(projectId, excelWriter, sheetTask);
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
     * @param range
     *
     * @return SheetTask 列表
     */
    protected abstract List<SheetTask> getSheetTasks(String projectId, Range range);
}
