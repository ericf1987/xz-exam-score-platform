package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.lang.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExportExcelController {

    @Autowired
    private ExcelReportManager excelReportManager;

    @PostMapping("/export/excel")
    @ResponseBody
    public Result exportExcel(@RequestParam("projectId") String projectId) {

        if (this.excelReportManager.isRunning(projectId)) {
            return Result.fail("正在执行 Excel 生成。");
        }

        this.excelReportManager.generateReports(projectId, false);
        return Result.success();
    }
}
