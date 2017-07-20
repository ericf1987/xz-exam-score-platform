package com.xz.scorep.executor.exportaggrdata.controller;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.exportaggrdata.service.AggregationDataExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用于在服务器测试相关数据
 *
 * @author luckylo
 * @createTime 2017-07-19.
 */
@Controller
public class ExportController {

    @Autowired
    private AggregationDataExport export;

    @PostMapping("/export/json")
    @ResponseBody
    public Result exportAggrData(@RequestParam("projectId") String projectId) {
        Runnable runnable = () -> export.exportData(projectId, false);
        new Thread(runnable).start();
        return Result.success();
    }
}
