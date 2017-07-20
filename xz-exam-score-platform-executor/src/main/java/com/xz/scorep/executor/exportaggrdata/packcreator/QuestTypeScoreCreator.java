package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class QuestTypeScoreCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "quest_type_score.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<Map<String, Object>> questTypeScores = context.getQuestTypeScores();
        return Collections.singletonList(packEntry(questTypeScores));
    }

    private EntryData packEntry(List<Map<String, Object>> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }

}
