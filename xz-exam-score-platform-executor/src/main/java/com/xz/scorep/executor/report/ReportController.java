package com.xz.scorep.executor.report;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportManager reportManager;

    @Autowired
    private ReportArchiveService reportArchiveService;

    /**
     * 查询页面报表内容
     *
     * @param projectId  项目ID
     * @param schoolId   学校ID（可选，如果为空则传 "-"）
     * @param reportName 报表名称
     *
     * @return 报表内容
     */
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

    /**
     * 报表打包
     *
     * @param projectId 项目ID
     *
     * @return 接收命令的结果（通常为成功）
     */
    @PostMapping("/report/archive/{projectId}")
    @ResponseBody
    public Result archiveReport(
            @PathVariable("projectId") String projectId
    ) {
        return archiveReport(projectId, null);
    }

    /**
     * 报表打包
     *
     * @param projectId 项目ID
     * @param subjectId 科目ID
     *
     * @return 接收命令的结果（通常为成功）
     */
    @PostMapping("/report/archive/{projectId}/{subjectId}")
    @ResponseBody
    public Result archiveReport(
            @PathVariable("projectId") String projectId,
            @PathVariable("subjectId") String subjectId
    ) {
        if (StringUtil.isEmpty(subjectId)) {
            reportArchiveService.startProjectArchive(projectId);
        } else {
            reportArchiveService.startSubjectArchive(projectId, subjectId);
        }
        return Result.success();
    }

    /**
     * 查询报表打包状态
     *
     * @param projectId 项目ID
     *
     * @return status=打包状态；url=打包地址
     */
    @GetMapping("/report/archive-status/{projectId}")
    @ResponseBody
    public Result archiveReportStatus(
            @PathVariable("projectId") String projectId
    ) {
        return archiveReportStatus(projectId, null);
    }

    /**
     * 查询报表打包状态
     *
     * @param projectId 项目ID
     * @param subjectId 科目ID
     *
     * @return status=打包状态；url=打包地址
     */
    @GetMapping("/report/archive-status/{projectId}/{subjectId}")
    @ResponseBody
    public Result archiveReportStatus(
            @PathVariable("projectId") String projectId,
            @PathVariable("subjectId") String subjectId
    ) {
        ArchiveStatus status;
        String url;

        if (StringUtil.isEmpty(subjectId)) {
            status = reportArchiveService.getProjectArchiveStatus(projectId);
            url = reportArchiveService.getProjectArchiveUrl(projectId);
        } else {
            status = reportArchiveService.getSubjectArchiveStatus(projectId, subjectId);
            url = reportArchiveService.getSubjectArchiveUrl(projectId, subjectId);
        }

        return Result.success()
                .set("status", status.name())
                .set("url", url);
    }
}
