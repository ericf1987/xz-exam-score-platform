package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.project.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计客观题选项选择率
 *
 * @author yidin
 */
@Component
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(6)
public class ObjectiveOptionAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectiveOptionAggregator.class);

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private QuestService questService;

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportCacheInitializer reportCache;

    @Autowired
    private SubjectService subjectService;


    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        String projectId = aggregateParameter.getProjectId();

        reportCache.initReportScoreCache(projectId, true); // 缓存客观题分数详情
        studentService.cacheStudents(projectId);                        // 缓存学生列表

        try {
            aggregate0(projectId);
        } catch (Exception e) {
            LOG.error("客观题选项统计失败", e);
        }
    }

    private CounterMap<List<String>> createCounterMap(String projectId) {
        int classCount = classService.listClasses(projectId).size();
        int schoolCount = schoolService.listSchool(projectId).size();
        int questCount = questService.queryQuests(projectId).size();

        // 算出近似有多少个计数器
        int approximateCounterMapSize = (classCount + schoolCount) * questCount * 4;
        return new CounterMap<>(approximateCounterMapSize);
    }

    private void aggregate0(String projectId) {

        LOG.info("开始统计客观题选项选率....");
        CounterMap<List<String>> objectiveCounterMap = createCounterMap(projectId);
        CounterMap<List<String>> studentCounterMap = new CounterMap<>();

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

        projectDao.execute("truncate table objective_option_rate");  // 清空数据

        //////////////////////////////////////////////////////////////

        SimpleCache reportCache = cacheFactory.getReportCache(projectId);
        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            LOG.info("正在统计科目{}客观题选率.....", subjectId);
            List<ExamQuest> quests = questService.queryQuests(projectId, subjectId, true);

            quests.forEach(quest -> {
                String questId = quest.getId();
                String cacheKey = "quest_" + questId;
                List<Row> scoreList = reportCache.get(cacheKey);

                scoreList.forEach(score -> {
                    String studentId = score.getString("student_id");
                    Row student = studentService.findStudent(projectId, studentId);

                    //该学生在基础表有无数据,但在分数表有(建项目只有该学生被删除)
                    if (student == null) {
                        return;
                    }

                    // 如果考生在该科目没有分数记录（因缺考或得零分而被排除），则忽略
                    Set<String> ignoreStudents = ignoreSubjectStudentMap.get(subjectId);
                    if (ignoreStudents != null && ignoreStudents.contains(studentId)) {
                        return;
                    }

                    // 提取考生答题选项
                    List<String> options = new ArrayList<>();

                    // 如果考生答题内容非法(或"*") 则为不选率
                    String stuAnswer = score.getString("objective_answer");
                    if (StringUtil.isBlank(stuAnswer) || stuAnswer.trim().equals("*")) {
                        options.add("*");
                    } else {
                        if (!quest.isMultiChoice() && stuAnswer.length() > 1) {//单选题含有多个选项,统计为不选!
                            options.add("*");
                        } else {
                            char[] chars = stuAnswer.toCharArray();
                            for (char c : chars) {
                                options.add(Character.toString(c));
                            }
                        }
                    }

                    // 添加到计数器
                    String classId = student.getString("class_id");
                    String schoolId = student.getString("school_id");
                    String provinceId = student.getString("province");

                    studentCounterMap.incre(Arrays.asList(questId, Range.CLASS, classId));
                    studentCounterMap.incre(Arrays.asList(questId, Range.SCHOOL, schoolId));
                    studentCounterMap.incre(Arrays.asList(questId, Range.PROVINCE, provinceId));

                    options.forEach(option -> {
                        objectiveCounterMap.incre(Arrays.asList(questId, option, Range.CLASS, classId));
                        objectiveCounterMap.incre(Arrays.asList(questId, option, Range.SCHOOL, schoolId));
                        objectiveCounterMap.incre(Arrays.asList(questId, option, Range.PROVINCE, provinceId));
                    });
                });
            });

            //////////////////////////////////////////////////////////////////////////
            MultipleBatchExecutor insertExecutor = new MultipleBatchExecutor(projectDao, 2000);

            objectiveCounterMap.forEach((key, count) -> {
                int studentCount = studentCounterMap.getCount(Arrays.asList(key.get(0), key.get(2), key.get(3)));
                int optionCount = objectiveCounterMap.getCount(key);
                double optionRate = (double) optionCount / studentCount;

                Map<String, Object> row = new HashMap<>();
                row.put("quest_id", key.get(0));
                row.put("option", key.get(1));
                row.put("range_type", key.get(2));
                row.put("range_id", key.get(3));
                row.put("option_count", optionCount);
                row.put("option_rate", optionRate);

                insertExecutor.push("objective_option_rate", row);
            });

            insertExecutor.finish();

        });

        LOG.info("客观题选项选率统计完成.....");

    }

}
