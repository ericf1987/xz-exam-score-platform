package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.TScore;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-24.
 */
public class TScoreCreator implements ScoreDataEntryCreator {

    public static final String ENTRY_NAME = "t_score.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<TScore> tScores = context.gettScores();
        return Collections.singletonList(packObj(tScores));
    }

    private EntryData packObj(List<TScore> scores) {
        return new EntryData(ENTRY_NAME, this.toBytes(scores));
    }
}
