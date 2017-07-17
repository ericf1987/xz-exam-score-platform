package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.Average;
import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/17.
 */
public class AverageCreator implements ScoreDataEntryCreator{

    public static final String ENTRY_NAME = "average.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {
        List<Average> averages = creatorContext.getAverages();

        return Collections.singletonList(packEntry(averages));
    }

    private EntryData packEntry(List<Average> averages) {
        return new EntryData(ENTRY_NAME, this.toBytes(averages));
    }
}
