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

    private static final Logger LOG = LoggerFactory.getLogger(ExportExcelController.class);

    @Autowired
    private ExcelReportManager excelReportManager;

    @PostMapping("/export/excel")
    @ResponseBody
    //该接口  网页平台并没直接调用,(只在测试页面有过调用)
    public Result exportExcel(@RequestParam("projectId") String projectId) {
        try {
            excelReportManager.generateReports(projectId, true, false);
            return Result.success();
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/export/status/{projectId}")
    @ResponseBody
    //该接口  网页平台并没直接调用
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
