package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.bean.ObjCorrectMap;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class ObjCorrectMapCreator implements ScoreDataEntryCreator {

    private static final String ENTRY_NAME = "obj_correct_map.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<ObjCorrectMap> maps = context.getObjCorrectMaps();
        return Collections.singletonList(packEntry(maps));
    }

    private EntryData packEntry(List<ObjCorrectMap> maps) {
        return new EntryData(ENTRY_NAME, this.toBytes(maps));
    }
}
