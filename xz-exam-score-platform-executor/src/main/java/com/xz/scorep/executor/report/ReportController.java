package com.xz.scorep.executor.report;

import com.xz.ajiaedu.common.lang.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ReportController {

    @Autowired
    private ReportManager reportManager;

    @GetMapping("/report/{projectId}/{schoolId}/{reportName}")
    @ResponseBody
    public Result getReport(@PathVariable("projectId") String projectId,
                            @PathVariable("schoolId") String schoolId,
                            @PathVariable("reportName") String reportName) {

        AbstractReport report = reportManager.getReport(reportName);

        if (report == null) {
            return Result.fail(404, "没有找到报表 " + reportName);
        }

        Map<?, ?> reportContent = report.generateReport(projectId, schoolId);
        return Result.success().set("report", reportContent);
    }
}
