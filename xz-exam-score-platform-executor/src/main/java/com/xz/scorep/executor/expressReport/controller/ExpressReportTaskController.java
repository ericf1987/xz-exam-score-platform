package com.xz.scorep.executor.expressReport.controller;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.expressReport.manager.ExpressReportManager;
import com.xz.scorep.executor.expressReport.service.ExpressReportTaskService;
import com.xz.scorep.executor.expressReport.service.PressureTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author by fengye on 2017/7/12.
 */
@Controller
public class ExpressReportTaskController {

    @Autowired
    ExpressReportTaskService expressReportTaskService;

    @Autowired
    ExpressReportManager expressReportManager;

    @Autowired
    PressureTestService pressureTestService;

    @PostMapping("/expressReport/task/start")
    @ResponseBody
    public Result startExpressReportTask(
            @RequestParam("projectId") String projectId,
            @RequestParam("subjectId") String subjectId
    ){
        expressReportManager.startTask(projectId, subjectId);
        return Result.success();
    }

    @PostMapping("/expressReport/task/school")
    @ResponseBody
    public Result startExpressReportBySchool(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId
    ){
        return Result.success();
    }

    @PostMapping("/expressReport/pressure/test1")
    @ResponseBody
    public Result startPressureTest1(
            @RequestParam("pdfName") String pdfName,
            @RequestParam("relativePath") String relativePath,
            @RequestParam("createUrl") String createUrl,
            @RequestParam("threadCount") String threadCount
    ){
        pressureTestService.startPressureTest1(pdfName, relativePath, createUrl, threadCount);
        return Result.success();
    }

    @PostMapping("/expressReport/pressure/test2")
    @ResponseBody
    public Result startPressureTest2(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId
    ){
        pressureTestService.startPressureTest2(projectId, schoolId, classId, subjectId);
        return Result.success();
    }
}
