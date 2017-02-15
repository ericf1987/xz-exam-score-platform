package com.xz.scorep.executor.exportexcel;


import com.xz.scorep.executor.bean.Range;

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

    public ReportTask(ReportGenerator reportGenerator, String category, String filename, Range range) {
        this.reportGenerator = reportGenerator;
        this.category = category;
        this.filename = filename;
        this.range = range;
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

    @Override
    public String toString() {
        return "ReportTask{" +
                "reportGenerator=" + reportGenerator.getClass().getSimpleName() +
                ", category='" + category + '\'' +
                ", filename='" + filename + '\'' +
                ", range=" + range +
                '}';
    }
}
