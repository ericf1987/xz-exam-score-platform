package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author luckylo
 * @createTime 2017-07-28.
 */
public class TopStudentListCreator implements ScoreDataEntryCreator {

    private static final String ENTRY_NAME = "top_student_list.json";

    @Override
    public List<EntryData> createEntries(CreatorContext context) {
        List<Map<String, Object>> topStudentLists = context.getTopStudentLists();
        return Collections.singletonList(packObj(topStudentLists));
    }

    private EntryData packObj(List<Map<String, Object>> list) {
        return new EntryData(ENTRY_NAME, this.toBytes(list));
    }
}
