package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.utils.SqlUtils.GroupType;

/**
 * 需要关注的小题,班级和学校得分率分差绝对值较大的小题,用于快报
 * @author by fengye on 2017/7/7.
 */
@Component
public class QuestToBeAttentionQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    TopScoreRateQuery topScoreRateQuery;

    @Autowired
    QuestService questService;

    public List<Row> combineByRange(List<Row> classData, List<Row> schoolData){
        List<Row> rows = topScoreRateQuery.combineByRange(classData, schoolData);

        //按照班级学校平均得分率的差值的绝对值排序
        rows.forEach(r -> {
            r.put("dValue", DoubleUtils.round(Math.abs(r.getDouble("rate", 0) - r.getDouble("parent_rate", 0))));
            r.put("parent_rate", DoubleUtils.toPercent(r.getDouble("parent_rate", 0)));
            r.put("rate", DoubleUtils.toPercent(r.getDouble("rate", 0)));
        });

        return rows.stream().sorted((Row r1, Row r2) -> {
            Double d1 = r1.getDouble("dValue", 0);
            Double d2 = r2.getDouble("dValue", 0);
            return d2.compareTo(d1);
        }).limit(5).collect(Collectors.toList());
    }

    public List<Row> queryToBeAttentionQuest(String projectId, String subjectId, String rangeName, String rangeId, List<ExamQuest> examQuests){
        return topScoreRateQuery.getScoreRate(projectId, subjectId, rangeName, rangeId, examQuests, true, GroupType.AVG);
    }
}
