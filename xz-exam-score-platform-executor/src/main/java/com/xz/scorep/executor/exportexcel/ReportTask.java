package com.xz.scorep.executor.exportexcel;


import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

/**
 * (description)
 * created at 16/05/31
 *
 * @author yiding_he
 */
public class ReportTask {

    private ReportGenerator reportGenerator;

    private String category;

    private String filename;

    private Range range;

    private Target target;

    public ReportTask(ReportGenerator reportGenerator, String category, String filename, Range range, Target target) {
        this.reportGenerator = reportGenerator;
        this.category = category;
        this.filename = filename;
        this.range = range;
        this.target = target;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public ReportGenerator getReportGenerator() {
        return reportGenerator;
    }

    public void setReportGenerator(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePathWithRange() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "ReportTask{" +
                "reportGenerator=" + reportGenerator +
                ", category='" + category + '\'' +
                ", filename='" + filename + '\'' +
                ", range=" + range +
                ", target=" + target +
                '}';
    }
}
