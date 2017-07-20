package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.RankLevelMap;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankLevelMapCreator implements ScoreDataEntryCreator{
    public static final String ENTRY_NAME = "rank_level_map.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {

        List<RankLevelMap> RankLevelMaps = creatorContext.getRankLevelMaps();

        return Collections.singletonList(packEntry(RankLevelMaps));
    }

    private EntryData packEntry(List<RankLevelMap> rankLevels) {
        return new EntryData(ENTRY_NAME, this.toBytes(rankLevels));
    }
}
