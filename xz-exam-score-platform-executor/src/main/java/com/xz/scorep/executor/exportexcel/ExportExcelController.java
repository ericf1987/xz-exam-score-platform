package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.utils.AsyncCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExportExcelController {
    private final Logger LOG = LoggerFactory.getLogger(ExportExcelController.class);

    @Autowired
    private ExcelReportManager excelReportManager;

    @PostMapping("/export/excel")
    @ResponseBody
    public Result exportExcel(@RequestParam("projectId") String projectId) {

        if (this.excelReportManager.isRunning(projectId)) {
            LOG.info("项目ID{}Excel报表正在生成,请勿重复导出Excel报表...", projectId);
            return Result.fail(1,"正在执行 Excel 生成,请耐心等待...。");
        }

        this.excelReportManager.generateReports(projectId, false);
        return Result.success();
    }

    @GetMapping("/export/status/{projectId}")
    @ResponseBody
    public Result queryStatus(@PathVariable("projectId") String projectId) {
        AsyncCounter counter = excelReportManager.getCounter(projectId);
        if (counter == null) {
            return Result.fail(1, "生成报表完成");
        }
        return Result.success()
                .set("total", counter.getTotal())
                .set("current", counter.getCurrentCount());
    }
}
