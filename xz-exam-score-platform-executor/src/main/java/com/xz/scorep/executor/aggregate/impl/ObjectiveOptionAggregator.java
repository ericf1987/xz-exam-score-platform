package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.utils.AsyncCounter;
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

    private static final String PROVINCE_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'province' as range_type,\n" +
            "  '{{province}}' as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select \n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score\n" +
            "  group by objective_answer\n" +
            ") a, (\n" +
            "  select count(1) as total from student\n" +
            ") b";

    private static final String SCHOOL_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'school' as range_type,\n" +
            "  a.school_id as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select\n" +
            "    student.school_id,\n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score,student\n" +
            "  where score.student_id=student.id\n" +
            "  group by school_id, objective_answer\n" +
            ") a, (\n" +
            "  select school_id, count(1) as total from student\n" +
            "  group by school_id\n" +
            ") b\n" +
            "where a.school_id=b.school_id";

    private static final String CLASS_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'class' as range_type,\n" +
            "  a.class_id as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select\n" +
            "    student.class_id,\n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score,student\n" +
            "  where score.student_id=student.id\n" +
            "  group by class_id, objective_answer\n" +
            ") a, (\n" +
            "  select class_id, count(1) as total from student\n" +
            "  group by class_id\n" +
            ") b\n" +
            "where a.class_id=b.class_id";


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
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        //<省(校,班),学生列表>
        Map<String, Map<String, List<String>>> studentList = getStudentList(projectId);

        //初始化缓存.....
        reportCache.initReportCache(projectId);

        ThreadPools.createAndRunThreadPool(aggregateConfig.getOptionPoolSize(), 1, pool -> {
            try {
                aggregate0(pool, projectId, studentList);
            } catch (Exception e) {
                LOG.error("客观题选项统计失败", e);
            }
        });

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


    private void aggregate0(ThreadPoolExecutor pool, String projectId, Map<String, Map<String, List<String>>> studentList) {
        List<ExamQuest> objectiveQuest = questService.queryQuests(projectId, true);
        String province = Range.PROVINCE_RANGE.getId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table objective_option_rate");
        LOG.info("客观题选项统计结果已清空。");

        //所有客观题
        LOG.info("客观题选率统计开始........");
        AsyncCounter asyncCounter = new AsyncCounter("客观题选项统计", objectiveQuest.size());

        for (ExamQuest quest : objectiveQuest) {
            String questId = quest.getId();

            pool.submit(() -> {
                try {
                    aggregateObjectiveOptions(projectId, studentList, province, projectDao, questId);
                } catch (Exception e) {
                    LOG.error("客观题选项统计失败", e);
                } finally {
                    asyncCounter.count();
                }
            });



//        for (ExamQuest quest : examQuests) {
//
//            String provinceSql = PROVINCE_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId()).replace("{{province}}", province);
//            String schoolSql = SCHOOL_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId());
//            String classSql = CLASS_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId());
//
//            pool.submit(() -> {
//                try {
//                    projectDao.execute(provinceSql);
//                    projectDao.execute(schoolSql);
//                    projectDao.execute(classSql);
//                } catch (DAOException e) {
//                    LOG.error("客观题选项统计失败", e);
//                } finally {
//                    counter.count();
//                }
//            });
//        }
        }
        LOG.info("客观题选率统计完成........");
    }

    private void aggregateObjectiveOptions(String projectId, Map<String, Map<String, List<String>>> studentList, String province, DAO projectDao, String questId) {
        for (Map.Entry<String, Map<String, List<String>>> entry : studentList.entrySet()) {
            String key = entry.getKey();
            Map<String, List<String>> value = entry.getValue();

            if (key.equals("province")) {
                List<String> provinceStudent = value.get(province);
                List<Row> rows = reportCache.queryObjectiveQuestAllStudentScore(projectId, questId);

                CounterMap<String> counterMap = new CounterMap();

                calculateOptionsCount(rows, counterMap);

                int studentCount = provinceStudent.size();
                for (Map.Entry<String, Integer> counter : counterMap.entrySet()) {
                    String counterKey = counter.getKey();
                    int counterValue = counter.getValue();
                    double rate = (counterValue * 1.0) / studentCount;
                    projectDao.execute(INSERT_RECORD, questId, counterKey, "province", province, counterValue, rate);

                }

            }

            if (key.equals("school")) {
                for (Map.Entry<String, List<String>> schoolStudentList : value.entrySet()) {
                    String schoolId = schoolStudentList.getKey();
                    List<String> schoolStudent = schoolStudentList.getValue();
                    List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, schoolStudent, questId);
                    CounterMap<String> schoolCounterMap = new CounterMap();

                    calculateOptionsCount(rows, schoolCounterMap);

                    int studentCount = schoolStudent.size();
                    for (Map.Entry<String, Integer> counter : schoolCounterMap.entrySet()) {
                        String counterKey = counter.getKey();
                        int counterValue = counter.getValue();
                        double rate = (counterValue * 1.0) / studentCount;
                        projectDao.execute(INSERT_RECORD, questId, counterKey, "school", schoolId, counterValue, rate);

                    }

                }
            }

            if (key.equals("class")) {
                for (Map.Entry<String, List<String>> classStudentList : value.entrySet()) {
                    String classId = classStudentList.getKey();
                    List<String> claSSStudent = classStudentList.getValue();
                    List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, claSSStudent, questId);
                    CounterMap<String> classCounterMap = new CounterMap();

                    calculateOptionsCount(rows, classCounterMap);

                    int studentCount = claSSStudent.size();
                    for (Map.Entry<String, Integer> counter : classCounterMap.entrySet()) {
                        String counterKey = counter.getKey();
                        int counterValue = counter.getValue();
                        double rate = (counterValue * 1.0) / studentCount;
                        projectDao.execute(INSERT_RECORD, questId, counterKey, "class", classId, counterValue, rate);

                    }
                }
            }
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
