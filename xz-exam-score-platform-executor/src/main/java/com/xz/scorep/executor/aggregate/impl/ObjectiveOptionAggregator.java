package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static String QUERY_PROVINCE_STUDENT_COUNT = "select COUNT(1) as student_count from `score_objective_{{subjectId}}`";

    private static String QUERY_SCHOOL_STUDENT_COUNT = "select student.school_id as school_id,\n" +
            "COUNT(`score_objective_{{subjectId}}`.student_id) as student_count\n" +
            "from student\n" +
            "LEFT JOIN\n" +
            "`score_objective_{{subjectId}}` on `score_objective_{{subjectId}}`.student_id = student.id\n" +
            "GROUP BY student.school_id";

    private static String QUERY_CLASS_STUDENT_COUNT = "select student.class_id as class_id,\n" +
            "COUNT(`score_objective_001`.student_id) as student_count\n" +
            "from student\n" +
            "LEFT JOIN\n" +
            "`score_objective_{{subjectId}}` on `score_objective_{{subjectId}}`.student_id = student.id\n" +
            "GROUP BY student.class_id";


    private static String QUERY_PROVINCE_STUDENT_LIST = "select student.id as student_id from student";

    private static String QUERY_SCHOOL_STUDENT_LIST = "select student.id as student_id from \n" +
            "student\n" +
            "where student.school_id = '{{schoolId}}'";

    private static String QUERY_CLASS_STUDENT_LIST = "select student.id as student_id from \n" +
            "student\n" +
            "where student.class_id = '{{classId}}'";

    private static String INSERT_RECORD = "insert into objective_option_rate values (?,?,?,?,?,?)";

    @Autowired
    private QuestService questService;

    @Autowired
    private AggregateConfig aggregateConfig;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportCacheInitializer reportCache;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        //<科目,<省(校,班),学生数>>
        Map<String, Map<String, Integer>> subjectsStudentCount = getSubjectsStudentCount(projectId);

        //<省(校,班),<id,学生列表>>
        Map<String, Map<String, List<String>>> studentList = getStudentList(projectId);

        //初始化缓存.....
        reportCache.initReportCache(projectId);

        ThreadPools.createAndRunThreadPool(aggregateConfig.getOptionPoolSize(), 1, pool -> {
            try {
                aggregate0(pool, projectId, subjectsStudentCount, studentList);
            } catch (Exception e) {
                LOG.error("客观题选项统计失败", e);
            }
        });

    }

    /**
     * 每个科目的参考学生数
     *
     * @param projectId
     * @return
     */
    private Map<String, Map<String, Integer>> getSubjectsStudentCount(String projectId) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        String province = Range.PROVINCE_RANGE.getId();

        DAO projectDao = daoFactory.getProjectDao(projectId);
        subjectService.listSubjects(projectId)
                .forEach(subject -> {
                    String subjectId = subject.getId();
                    Map<String, Integer> map = new HashMap<>();
                    //总体参考人数
                    int provinceCount = projectDao.queryFirst(QUERY_PROVINCE_STUDENT_COUNT
                            .replace("{{subjectId}}", subjectId))
                            .getInteger("student_count", 0);
                    map.put(province, provinceCount);

                    //学校学生参考人数
                    projectDao.query(QUERY_SCHOOL_STUDENT_COUNT
                            .replace("{{subjectId}}", subjectId))
                            .forEach(row -> {
                                String schoolId = row.getString("school_id");
                                map.put(schoolId, row.getInteger("student_count", 0));
                            });

                    //班级学生参考人数
                    projectDao.query(QUERY_CLASS_STUDENT_COUNT
                            .replace("{{subjectId}}", subjectId))
                            .forEach(row -> {
                                String classId = row.getString("class_id");
                                map.put(classId, row.getInteger("student_count", 0));
                            });

                    result.put(subjectId, map);

                });


        return result;
    }

    private Map<String, Map<String, List<String>>> getStudentList(String projectId) {
        String province = Range.PROVINCE_RANGE.getId();

        Map<String, Map<String, List<String>>> result = new HashMap<>();

        Map<String, List<String>> provinceMap = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        //省学生列表
        List<String> provinceStudent = projectDao
                .query(QUERY_PROVINCE_STUDENT_LIST)
                .stream()
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());
        provinceMap.put(province, provinceStudent);
        result.put("province", provinceMap);

        //校学生列表
        Map<String, List<String>> schoolMap = new HashMap<>();
        schoolService.listSchool(projectId)
                .forEach(school -> {
                    String schoolId = school.getId();
                    List<String> schoolStudent = projectDao.query(QUERY_SCHOOL_STUDENT_LIST
                            .replace("{{schoolId}}", schoolId))
                            .stream()
                            .map(row -> row.getString("student_id"))
                            .collect(Collectors.toList());
                    schoolMap.put(schoolId, schoolStudent);
                });
        result.put("school", schoolMap);

        //班学生列表
        Map<String, List<String>> classMap = new HashMap<>();
        classService.listClasses(projectId)
                .forEach(clazz -> {
                    String clazzId = clazz.getId();
                    List<String> classStudent = projectDao.query(QUERY_CLASS_STUDENT_LIST.replace("{{classId}}", clazzId))
                            .stream()
                            .map(row -> row.getString("student_id"))
                            .collect(Collectors.toList());
                    classMap.put(clazzId, classStudent);
                });
        result.put("class", classMap);

        return result;
    }


    private void aggregate0(ThreadPoolExecutor pool, String projectId, Map<String, Map<String, Integer>> subjectsStudentCount, Map<String, Map<String, List<String>>> studentList) {
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);


        String province = Range.PROVINCE_RANGE.getId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table objective_option_rate");
        LOG.info("客观题选项统计结果已清空。");

        //所有客观题
        LOG.info("客观题选率统计开始........");

        subjects.forEach(subject -> {

            String subjectId = subject.getId();
            Map<String, Integer> studentCount = subjectsStudentCount.get(subjectId);

            List<ExamQuest> objectiveQuest = questService.queryQuests(projectId, subjectId, true);
            LOG.info("正在统计项目ID{},科目ID{}客观题选率.....", projectId, subjectId);

            objectiveQuest.forEach(quest -> {
                String questId = quest.getId();
                aggregateObjectiveOptions(pool, projectId, studentList, province, projectDao, questId, studentCount);
            });

            LOG.info("统计项目ID{},科目ID{}客观题选率完成.....", projectId, subjectId);

        });

        LOG.info("客观题选率统计完成........");
    }

    private void aggregateObjectiveOptions(ThreadPoolExecutor pool, String projectId, Map<String, Map<String, List<String>>> studentList, String province, DAO projectDao, String questId, Map<String, Integer> studentCountMap) {
        for (Map.Entry<String, Map<String, List<String>>> entry : studentList.entrySet()) {
            String key = entry.getKey();
            Map<String, List<String>> value = entry.getValue();

            if (key.equals("province")) {
                pool.submit(() -> {
                    List<Row> rows = reportCache.queryObjectiveQuestAllStudentScore(projectId, questId);

                    CounterMap<String> counterMap = new CounterMap();

                    calculateOptionsCount(rows, counterMap);
                    calculateOptionsRate(province, projectDao, questId, studentCountMap, counterMap, "province");
                });

            }

            if (key.equals("school")) {
                for (Map.Entry<String, List<String>> schoolStudentList : value.entrySet()) {
                    pool.submit(() -> {
                        String schoolId = schoolStudentList.getKey();
                        List<String> schoolStudent = schoolStudentList.getValue();
                        List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, schoolStudent, questId);
                        CounterMap<String> schoolCounterMap = new CounterMap();

                        calculateOptionsCount(rows, schoolCounterMap);
                        calculateOptionsRate(schoolId, projectDao, questId, studentCountMap, schoolCounterMap, "school");

                    });
                }
            }

            if (key.equals("class")) {
                for (Map.Entry<String, List<String>> classStudentList : value.entrySet()) {
                    pool.submit(() -> {
                        String classId = classStudentList.getKey();
                        List<String> claSSStudent = classStudentList.getValue();
                        List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, claSSStudent, questId);
                        CounterMap<String> classCounterMap = new CounterMap();

                        calculateOptionsCount(rows, classCounterMap);
                        calculateOptionsRate(classId, projectDao, questId, studentCountMap, classCounterMap, "class");
                    });
                }
            }
        }
    }

    private void calculateOptionsRate(String rangId, DAO projectDao, String questId, Map<String, Integer> studentCountMap, CounterMap<String> counterMap, String rangType) {
        int studentCount = studentCountMap.get(rangId);
        for (Map.Entry<String, Integer> counter : counterMap.entrySet()) {
            String option = counter.getKey();
            int optionCount = counter.getValue();
            double rate = (optionCount * 1.0) / studentCount;
            projectDao.execute(INSERT_RECORD, questId, option, rangType, rangId, optionCount, rate);

        }
    }

    private void calculateOptionsCount(List<Row> rows, CounterMap<String> counterMap) {
        rows.forEach(row -> {
            String answer = row.getString("objective_answer");
            if (answer == null) {
                counterMap.incre(null, 1);
                return;
            }
            if ("*".equals(answer)) {
                counterMap.incre("*", 1);
                return;
            }
            char[] chars = answer.toUpperCase().toCharArray();
            for (Character ch : chars) {
                String string = String.valueOf(ch);
                counterMap.incre(string, 1);
            }
        });

    }
}
