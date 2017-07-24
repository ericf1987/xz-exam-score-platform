package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.TopAverage;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-24.
 */
public class TopAverageCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "top_average.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<TopAverage> topAverages = context.getTopAverages();
        return Collections.singletonList(packObj(topAverages));
    }

    private EntryData packObj(List<TopAverage> topAverages) {
        return new EntryData(ENTRY_NAME, this.toBytes(topAverages));
    }
}
