package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ScoreSegmentCounter;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.utils.SqlUtils.replaceRangeId;
import static com.xz.scorep.executor.utils.SqlUtils.replaceSubjectId;

/**
 * 各分数段人数统计
 *
 * @author by fengye on 2017/7/4.
 */
@Component
public class ScoreSegmentQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SubjectService subjectService;

    public static final int SCORE_INTERVAL = 10;

    public static final int MIN_SCORE = 30;

    public static final String QUERY_SCORE = "select score.*, stu.{{range_id}}\n" +
            "FROM score_subject_{{subject_id}} score, student stu\n" +
            "WHERE score.`student_id` = stu.`id`\n" +
            "AND stu.{{range_id}} = ?";

    public ScoreSegmentCounter getScoreSegmentCounter(String projectId, String subjectId) {
        ExamSubject subject = subjectService.findSubject(projectId, subjectId);
        double fullScore = subject.getFullScore();
        return new ScoreSegmentCounter(projectId, null, Target.subject(subjectId), (int) fullScore, MIN_SCORE, SCORE_INTERVAL);
    }



    public List<String> getSegmentSpan(ScoreSegmentCounter scoreSegmentCounter) {
        int max = scoreSegmentCounter.getMax();
        int min = scoreSegmentCounter.getMin();
        int interval = scoreSegmentCounter.getInterval();

        List<String> column = new ArrayList<>();
        column.add(min + "以下");
        for (int i = min; i < max; i += interval){
            int j = i + interval;
            column.add(i + "-" + j);
        }
        column.add(max + "以上");
        return column;
    }

    public LinkedHashMap<Integer, Integer> getCountByScoreSegment(String projectId, String subjectId, String rangeName, String rangeId){
        DAO projectDao = daoFactory.getProjectDao(projectId);

        String sql = replaceRangeId(rangeName, replaceSubjectId(QUERY_SCORE, "{{subject_id}}", subjectId));

        List<Row> rows = projectDao.query(sql, rangeId);

        ScoreSegmentCounter scoreSegmentCounter = getScoreSegmentCounter(projectId, subjectId);

        addCounterMap(scoreSegmentCounter, rows);

        CounterMap<Integer> counterMap = scoreSegmentCounter.getCounterMap();

        return sortCounterMap(counterMap);

    }

    private LinkedHashMap<Integer, Integer> sortCounterMap(CounterMap<Integer> counterMap) {
        return counterMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
        ));
    }

    private void addCounterMap(ScoreSegmentCounter scoreSegmentCounter, List<Row> rows) {
        int max = scoreSegmentCounter.getMax();
        int min = scoreSegmentCounter.getMin();
        int interval = scoreSegmentCounter.getInterval();

        //[0, min)分数段的人数
        scoreSegmentCounter.addToCounter(0, (int)rows.stream().filter(r -> r.getDouble("score", 0) < min).count());

        //[min, max)分数段的人数
        for(int i = min; i <= max; i+= interval){
            int current = i;
            int next = i + interval;

            if(next > max){
                scoreSegmentCounter.addToCounter(max, (int)rows.stream().filter(r -> r.getDouble("score", 0) == max).count());
                continue;
            }

            scoreSegmentCounter.addToCounter(current, (int)rows.stream().filter(r -> r.getDouble("score", 0) >= current && r.getDouble("score", 0) < next).count() );
        }
    }

}
