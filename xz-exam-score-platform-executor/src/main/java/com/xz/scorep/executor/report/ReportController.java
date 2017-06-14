package com.xz.scorep.executor.report;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.AggregateStatus;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
    public static final String QUERY_AGGREGATION_STATUS = "select * from aggregation where subject_id = '{{subjectId}}' and project_id = '{{projectId}}' order by start_time desc";

    @Autowired
    private ReportManager reportManager;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportArchiveService reportArchiveService;

    /**
     * 查询页面报表内容
     *
     * @param projectId  项目ID
     * @param schoolId   学校ID（可选，如果为空则传 "-"）
     * @param reportName 报表名称
     * @return 报表内容
     */
    @GetMapping("/report/{projectId}/{schoolId}/{subjectId}/{reportName}")
    @ResponseBody
    public Result getReport(@PathVariable("projectId") String projectId,
                            @PathVariable("schoolId") String schoolId,
                            @PathVariable("subjectId") String subjectId,
                            @PathVariable("reportName") String reportName) {

        AbstractReport report = reportManager.getReport(reportName);

        if (report == null) {
            return Result.fail(404, "没有找到报表 " + reportName);
        }

        DAO managerDao = daoFactory.getManagerDao();
        subjectId = StringUtil.isEmpty(subjectId) ? "" : subjectId;
        Row row = managerDao.queryFirst(QUERY_AGGREGATION_STATUS
                .replace("{{projectId}}", projectId)
                .replace("{{subjectId}}", subjectId));
        if (!AggregateStatus.Finished.name().equals(row.getString("status"))) {
            return Result.fail(1, "项目正在统计,请稍后再试");
        }

        try {
            Map<?, ?> reportContent = report.generateReport(projectId, schoolId, subjectId);
            return Result.success().set("report", reportContent);
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.getMessage());
        }
    }


    /**
     * 报表打包
     *
     * @param projectId 项目ID
     * @param subjectId 科目ID
     * @return 接收命令的结果（通常为成功）
     */
    @PostMapping("/report/archive")
    @ResponseBody
    public Result archiveReport(
            @RequestParam("projectId") String projectId,
            @RequestParam(required = false, name = "subjectId") String subjectId
    ) {

        try {
            if (StringUtil.isEmpty(subjectId)) {
                reportArchiveService.startProjectArchive(projectId);
            } else {
                reportArchiveService.startSubjectArchive(projectId, subjectId);
            }

            return Result.success();
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询报表打包状态
     *
     * @param projectId 项目ID
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

        // 打包状态不分科目，正在打包就是正在打包
        status = reportArchiveService.getProjectArchiveStatus(projectId);

        if (StringUtil.isEmpty(subjectId)) {
            //全科报表  设置科目ID为000
            url = reportArchiveService.getSubjectArchiveUrl(projectId, "000");
        } else {
            url = reportArchiveService.getSubjectArchiveUrl(projectId, subjectId);
        }

        return Result.success()
                .set("status", status.name())
                .set("url", url);
    }
}
