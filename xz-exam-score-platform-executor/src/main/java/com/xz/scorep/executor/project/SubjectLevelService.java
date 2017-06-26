package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.DoubleCounterMap;
import com.xz.scorep.executor.bean.SubjectLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/6/26.
 */
@Service
public class SubjectLevelService {

    public void batchUpdateFullScore(DAO projectDao, DoubleCounterMap<SubjectLevel> subjectLevelFullScore) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SubjectLevel subjectLevel : subjectLevelFullScore.keySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("subject", subjectLevel.getSubject());
            m.put("level", subjectLevel.getLevel());
            m.put("full_score", subjectLevelFullScore.get(subjectLevel));
            resultList.add(m);
        }
        projectDao.insert(resultList, "subject_level");
    }
}
