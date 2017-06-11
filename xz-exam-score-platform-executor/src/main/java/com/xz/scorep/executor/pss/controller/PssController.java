package com.xz.scorep.executor.pss.controller;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.pss.mamage.PssTaskManager;
import com.xz.scorep.executor.pss.service.PssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author by fengye on 2017/5/24.
 */

@Controller
public class PssController {
    @Autowired
    PssService pssService;

    @Autowired
    PssTaskManager pssTaskManager;

    @PostMapping("/img/showImgReport")
    @ResponseBody
    public Result runTaskByClassAndSubject(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId
    ) {
        pssService.runTaskByClassAndSubject(projectId, schoolId, classId, subjectId, null);
        return Result.success();
    }

    @PostMapping("/img/task/start")
    @ResponseBody
    public Result startPssTask(
            @RequestParam("projectId") String projectId
    ) {
        pssTaskManager.startPssTask(projectId, null, true);
        return Result.success();
    }

    @PostMapping("/img/task/oneStudent")
    @ResponseBody
    public Result startPssTask(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("studentId") String studentId
    ) {
        pssService.runTaskByOneStudent(projectId, schoolId, classId, subjectId, studentId, null);
        return Result.success();
    }
}
