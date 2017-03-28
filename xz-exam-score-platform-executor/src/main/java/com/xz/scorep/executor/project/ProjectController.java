package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.aggregate.AggregationService;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.exportexcel.ExcelReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProjectController {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ExcelReportManager excelReportManager;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private ProjectService projectService;

    @ResponseBody
    @GetMapping("/project/status")
    public Result getProjectStatus(
            @RequestParam("projectId") String projectId
    ) {
        ExamProject project = projectService.findProject(projectId);
        boolean projectImported = project != null;

        boolean generatingReport;
        generatingReport = projectImported && excelReportManager.isRunning(projectId);

        boolean runningAggregation;
        runningAggregation = projectImported &&
                aggregationService.getAggregateByStatus(projectId, "Running") != null;

        boolean canRunAggregation = !generatingReport && !runningAggregation;
        LOG.info("projectImported={}, canRunAggregation={}", projectImported, canRunAggregation);

        boolean canQuery = project != null && project.getStatus().equals(ProjectStatus.Ready.name());

        return Result.success()
                .set("projectImported", projectImported)
                .set("generatingReport", generatingReport)
                .set("runningAggregation", runningAggregation)
                .set("canRunAggregation", canRunAggregation)
                .set("canQuery", canQuery);
    }
}
