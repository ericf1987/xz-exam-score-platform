package com.xz.scorep.executor.report;


import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

public abstract class AbstractReport {

    @Autowired
    private ReportManager reportManager;

    @PostConstruct
    private void initAbstractReport() {
        this.reportManager.register(this);
    }

    public abstract Map<?,?> generateReport(String projectId, String schoolId, String subjectId);
}
