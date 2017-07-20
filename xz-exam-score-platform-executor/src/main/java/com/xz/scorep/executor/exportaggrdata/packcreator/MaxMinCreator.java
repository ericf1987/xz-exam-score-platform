package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.MaxMin;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * 项目最大最小分
 *
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class MaxMinCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "score_minmax.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {
        List<MaxMin> minMaxes = creatorContext.getMaxMins();
        return Collections.singletonList(packEntry(minMaxes));
    }

    private EntryData packEntry(List<MaxMin> minMaxes) {
        return new EntryData(ENTRY_NAME, this.toBytes(minMaxes));
    }
}
