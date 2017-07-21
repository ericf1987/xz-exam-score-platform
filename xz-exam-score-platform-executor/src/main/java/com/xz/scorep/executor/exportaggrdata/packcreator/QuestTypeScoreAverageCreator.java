package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.QuestTypeScoreAverage;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-21.
 */
public class QuestTypeScoreAverageCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "quest_type_score_average.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<QuestTypeScoreAverage> scoreAverages = context.getQuestTypeScoreAverages();
        return Collections.singletonList(packObj(scoreAverages));
    }

    private EntryData packObj(List<QuestTypeScoreAverage> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
