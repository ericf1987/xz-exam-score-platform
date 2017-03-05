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

    private Class<? extends ReportGenerator> generatorClass;

    private String category;

    private String filename;

    private Range range;

    private Target target;

    public ReportTask(Class<? extends ReportGenerator> generatorClass, String category, String filename, Range range, Target target) {
        this.generatorClass = generatorClass;
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

    public Class<? extends ReportGenerator> getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(Class<? extends ReportGenerator> generatorClass) {
        this.generatorClass = generatorClass;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFileName() {
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
                "generatorClass=" + generatorClass +
                ", category='" + category + '\'' +
                ", filename='" + filename + '\'' +
                ", range=" + range +
                ", target=" + target +
                '}';
    }
}
