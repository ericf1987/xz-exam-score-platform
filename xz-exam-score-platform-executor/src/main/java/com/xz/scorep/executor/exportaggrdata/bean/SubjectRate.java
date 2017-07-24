package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/7/21.
 */
public class SubjectRate extends MongoAggrObject implements Serializable {

    private Range range;

    private List<Map<String, Object>> subjectRates = new ArrayList<>();

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public List<Map<String, Object>> getSubjectRates() {
        return subjectRates;
    }

    public SubjectRate() {
    }

    public SubjectRate(Range range, List<Map<String, Object>> subjectRates) {
        this.range = range;
        this.subjectRates = subjectRates;
    }

    @Override
    public String toString() {
        return "SubjectRate{" +
                "range=" + range +
                ", subjectRates=" + subjectRates +
                '}';
    }
}
