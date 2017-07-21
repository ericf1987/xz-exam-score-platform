package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.StdDeviation;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/21.
 */
public class StdDeviationCreator implements ScoreDataEntryCreator{
    public static final String ENTRY_NAME = "std_deviation.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<StdDeviation> stdDeviations = context.getStdDeviations();
        return Collections.singletonList(packEntry(stdDeviations));
    }

    private EntryData packEntry(List<StdDeviation> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
