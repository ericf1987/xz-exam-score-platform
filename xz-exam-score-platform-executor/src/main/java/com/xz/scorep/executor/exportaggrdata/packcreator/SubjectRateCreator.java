package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.SubjectRate;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/24.
 */
public class SubjectRateCreator implements ScoreDataEntryCreator{

    public static final String ENTRY_NAME = "subject_rate.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<SubjectRate> subjectRates = context.getSubjectRates();
        return Collections.singletonList(packEntry(subjectRates));
    }

    private EntryData packEntry(List<SubjectRate> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
