package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author luckylo
 * @createTime 2017-07-25.
 */
public class TotalScoreCreator implements ScoreDataEntryCreator {

    private static final String ENTRY_NAME = "total_score.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<Map<String, Object>> totalScores = context.getTotalScores();
        return Collections.singletonList(packObj(totalScores));
    }

    private EntryData packObj(List<Map<String, Object>> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
