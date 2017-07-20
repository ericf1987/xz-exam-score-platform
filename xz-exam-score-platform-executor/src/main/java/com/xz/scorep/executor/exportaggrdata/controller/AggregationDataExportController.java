package com.xz.scorep.executor.exportaggrdata.controller;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.exportaggrdata.service.AggregationDataExport;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author by fengye on 2017/7/19.
 */
@Controller
@RequestMapping("/export-data-dump")
public class AggregationDataExportController {
    @Autowired
    AggregationDataExport aggregationDataExport;

    @RequestMapping(value = "/exportDataDump", method = RequestMethod.POST)
    @ResponseBody
    public Result exportDataDump(
            @RequestParam("projectId") String projectId,
            @RequestParam("notifyImport") String notifyImport
    ){
        return aggregationDataExport.exportData(projectId, BooleanUtils.toBoolean(notifyImport));
    }
}
