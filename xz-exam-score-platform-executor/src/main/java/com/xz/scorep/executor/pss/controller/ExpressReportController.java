package com.xz.scorep.executor.pss.controller;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.server.express.ExpressReportServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author by fengye on 2017/7/10.
 */
@Controller
public class ExpressReportController {

    @Autowired
    ExpressReportServer expressReportServer;

    @PostMapping("/expressReport/data")
    @ResponseBody
    public Result getExpressReportData(
            @RequestParam(value = "projectId", required = true, defaultValue = "") String projectId,
            @RequestParam(value = "schoolId", required = true, defaultValue = "") String schoolId,
            @RequestParam(value = "classId", required = true, defaultValue = "") String classId,
            @RequestParam(value = "subjectId", required = true, defaultValue = "") String subjectId
    ) {
        Param param = new Param().setParameter("projectId", projectId)
                .setParameter("schoolId", schoolId)
                .setParameter("classId", classId)
                .setParameter("subjectId", subjectId);
        return expressReportServer.execute(param);
    }

}
