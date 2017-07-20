package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.OverAverage;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class OverAverageCreator implements ScoreDataEntryCreator {

    private static final String ENTRY_NAME = "over_average.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<OverAverage> overAverages = context.getOverAverages();
        return Collections.singletonList(packEntry(overAverages));
    }

    private EntryData packEntry(List<OverAverage> overAverages) {
        return new EntryData(ENTRY_NAME, this.toBytes(overAverages));
    }

}
