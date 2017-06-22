package com.xz.scorep.executor.pss.controller;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregationService;
import com.xz.scorep.executor.api.server.paperScreenShot.PaperImgServer;
import com.xz.scorep.executor.pss.mamage.PssTaskManager;
import com.xz.scorep.executor.pss.service.PssService;
import org.apache.commons.lang.StringUtils;
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

    @Autowired
    AggregationService aggregationService;

    @Autowired
    PaperImgServer paperImgServer;

    /**
     * 按照班级和科目生成
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @param classId   班级ID
     * @param subjectId 科目ID
     * @return
     */
    @PostMapping("/img/showImgReport")
    @ResponseBody
    public Result runTaskByClassAndSubject(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId
    ) {

        if (!canStartPssTask(projectId, AggregateType.Quick, subjectId)) {
            return Result.fail("项目还未统计完成，请稍后执行。。。");
        }

        pssService.runTaskByClassAndSubject(projectId, schoolId, classId, subjectId, null);
        return Result.success();
    }

    /**
     * 按照整个项目生成
     *
     * @param projectId 项目ID
     * @return
     */
    @PostMapping("/img/task/start")
    @ResponseBody
    public Result startPssTask(
            @RequestParam("projectId") String projectId,
            @RequestParam("subjectId") String subjectId
    ) {

        if (!canStartPssTask(projectId, AggregateType.Quick, subjectId)) {
            return Result.fail("项目还未统计完成，请稍后执行。。。");
        }

        pssTaskManager.startPssTask(projectId, subjectId, null, true);
        return Result.success();
    }

    /**
     * 生成单个学生
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @param classId   班级ID
     * @param subjectId 科目ID
     * @param studentId 学生ID
     * @return
     */
    @PostMapping("/img/task/oneStudent")
    @ResponseBody
    public Result startPssTask(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("studentId") String studentId
    ) {

        if (null == aggregationService.getAggregateStatus(projectId, AggregateType.Quick)) {
            return Result.fail("项目还未统计完成，请稍后执行。。。");
        }

        pssService.runTaskByOneStudent(projectId, schoolId, classId, subjectId, studentId, null);
        return Result.success();
    }

    /**
     * 将生成失败的学生重新生成
     *
     * @param projectId 项目ID
     * @return
     */
    @PostMapping("/img/task/regenerateFail")
    @ResponseBody
    public Result regenerateFail(
            @RequestParam("projectId") String projectId
    ) {

        if (null == aggregationService.getAggregateStatus(projectId, AggregateType.Quick)) {
            return Result.fail("项目还未统计完成，请稍后执行。。。");
        }

        pssService.regenerateFail(projectId);
        return Result.success();
    }

    @PostMapping("/img/task/data")
    @ResponseBody
    public Result queryPssData(
            @RequestParam("projectId") String projectId,
            @RequestParam("schoolId") String schoolId,
            @RequestParam("classId") String classId,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("studentId") String studentId
    ) {
        Param param = new Param().setParameter("projectId", projectId)
                .setParameter("schoolId", schoolId)
                .setParameter("classId", classId)
                .setParameter("subjectId", subjectId)
                .setParameter("studentId", studentId);
        return paperImgServer.execute(param);
    }

    public boolean canStartPssTask(String projectId, AggregateType aggregateType, String subjectId) {
        if (StringUtils.isBlank(subjectId)) {
            return null != aggregationService.getAggregateStatus(projectId, aggregateType);
        }
        Row r1 = aggregationService.getAggregateStatus(projectId, aggregateType, subjectId);
        Row r2 = aggregationService.getAggregateStatus(projectId, aggregateType);
        return null != r1 || null != r2;
    }
}
