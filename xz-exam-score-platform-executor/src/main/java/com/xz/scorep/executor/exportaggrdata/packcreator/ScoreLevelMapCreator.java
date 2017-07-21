package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreLevelMap;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class ScoreLevelMapCreator implements ScoreDataEntryCreator{
    public static final String ENTRY_NAME = "score_level_map.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<ScoreLevelMap> scoreLevelMaps = context.getScoreLevelMaps();
        return Collections.singletonList(packEntry(scoreLevelMaps));
    }

    private EntryData packEntry(List<ScoreLevelMap> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
