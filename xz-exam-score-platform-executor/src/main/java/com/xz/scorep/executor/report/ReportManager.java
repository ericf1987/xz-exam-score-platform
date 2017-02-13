package com.xz.scorep.executor.report;

import com.xz.ajiaedu.common.lang.StringUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportManager {

    private Map<String, AbstractReport> reportMappings = new HashMap<>();

    public void register(AbstractReport abstractReport) {
        String className = abstractReport.getClass().getSimpleName();
        String reportName;

        if (className.endsWith("Report")) {
            reportName = StringUtil.removeEnd(className, "Report");
        } else {
            throw new IllegalArgumentException("AbstractReport 的子类必须以 'Report' 结尾");
        }

        this.reportMappings.put(reportName, abstractReport);
    }

    public AbstractReport getReport(String reportName) {
        return this.reportMappings.get(reportName);
    }
}
