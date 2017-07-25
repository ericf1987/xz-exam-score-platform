package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreSegment;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/25.
 */
public class ScoreSegmentsCreator implements ScoreDataEntryCreator{

    public static final String ENTRY_NAME = "score_segment.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<ScoreSegment> scoreSegments = context.getScoreSegments();
        return Collections.singletonList(packEntry(scoreSegments));
    }

    private EntryData packEntry(List<ScoreSegment> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }

}
