package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.QuestDeviation;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class QuestDeviationCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "quest_deviation.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<QuestDeviation> questDeviations = context.getQuestDeviations();
        return Collections.singletonList(packEntry(questDeviations));
    }

    private EntryData packEntry(List<QuestDeviation> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }

}
