package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.RankSegment;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankSegmentCreator implements ScoreDataEntryCreator{
    public static final String ENTRY_NAME = "rank_segment.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {

        List<RankSegment> rankSegments = creatorContext.getRankSegments();

        return Collections.singletonList(packEntry(rankSegments));
    }

    private EntryData packEntry(List<RankSegment> rankSegments) {
        return new EntryData(ENTRY_NAME, this.toBytes(rankSegments));
    }
}
