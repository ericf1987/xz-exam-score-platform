package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.RankLevel;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankLevelCreator implements ScoreDataEntryCreator{
    public static final String ENTRY_NAME = "rank_level.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {

        List<RankLevel> rankLevels = creatorContext.getRankLevels();

        return Collections.singletonList(packEntry(rankLevels));
    }

    private EntryData packEntry(List<RankLevel> rankLevels) {
        return new EntryData(ENTRY_NAME, this.toBytes(rankLevels));
    }
}
