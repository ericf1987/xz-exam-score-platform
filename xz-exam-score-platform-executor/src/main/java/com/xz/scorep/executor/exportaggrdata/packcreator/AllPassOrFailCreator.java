package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.xz.scorep.executor.exportaggrdata.bean.AllPassOrFail;
import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author by fengye on 2017/7/17.
 */
@Component
public class AllPassOrFailCreator implements ScoreDataEntryCreator{

    public static final String ENTRY_NAME = "all_subject_pass_rate.json";

    @Override
    public List<EntryData> createEntries(CreatorContext creatorContext) {

        List<AllPassOrFail> allPassOrFails = creatorContext.getAllPassOrFails();

        return Collections.singletonList(packEntry(allPassOrFails));
    }

    private EntryData packEntry(List<AllPassOrFail> allPassOrFail) {
        return new EntryData(ENTRY_NAME, this.toBytes(allPassOrFail));
    }

}
