package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 统计客观题选项选择率
 *
 * @author yidin
 */
@Component
@AggregateTypes(AggregateType.Basic)
@AggragateOrder(6)
public class ObjectiveOptionAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectiveOptionAggregator.class);

    @Autowired
    private QuestService questService;

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private AggregateConfig aggregateConfig;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportCacheInitializer reportCache;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        String projectId = aggregateParameter.getProjectId();

        reportCache.initReportScoreCache(projectId, true);  // 缓存客观题分数详情

        ThreadPools.createAndRunThreadPool(aggregateConfig.getOptionPoolSize(), 1, pool -> {
            try {
                aggregate0(pool, projectId);
            } catch (Exception e) {
                LOG.error("客观题选项统计失败", e);
            }
        });

    }

    private void aggregate0(ThreadPoolExecutor pool, String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        //////////////////////////////////////////////////////////////

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);

        // Set 查找比 List 快，Collectors.toSet() 返回的是 HashSet
        Map<String, Set<String>> ignoreSubjectStudentMap = new HashMap<>();

        subjects.forEach(subject -> {
            String ignoreStudentSql = "select id from student where " +
                    "id not in (select student_id from score_subject_" + subject.getId() + ")";

            Set<String> ignoreStudents = projectDao.query(ignoreStudentSql)
                    .stream().map(row -> row.getString("id")).collect(Collectors.toSet());

            ignoreSubjectStudentMap.put(subject.getId(), ignoreStudents);
        });

        //////////////////////////////////////////////////////////////

        List<ExamQuest> quests = questService.queryQuests(projectId, true);
        SimpleCache reportCache = cacheFactory.getReportCache(projectId);

        quests.forEach(quest -> {
            String subjectId = quest.getExamSubject();
            String cacheKey = "quest_" + quest.getId();
            List<Row> scoreList = reportCache.get(cacheKey);

            scoreList.forEach(score -> {
                String studentId = score.getString("student_id");
                Set<String> ignoreStudents = ignoreSubjectStudentMap.get(subjectId);
                if (ignoreStudents.contains(studentId)) {
                    return;
                }

                String stuAnswer = score.getString("objective_answer");
                if (StringUtil.isBlank(stuAnswer) || stuAnswer.trim().equals("*")) {
                    return;
                }

                List<String> options = new ArrayList<>();
                char[] chars = stuAnswer.toCharArray();
                for (char c : chars) {
                    options.add(Character.toString(c));
                }

                // TODO 添加到计数器中
            });
        });
    }

}
