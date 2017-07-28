package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.ajiaedu.common.report.Keys.Range;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.utils.AsyncCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 客观题区分度
 * 区分度 D = (27%高分组的平均分 - 27%低分组的平均分) / 满分值
 * 该统计为向上取整 Math.ceil(double value)
 *
 * @author luckylo
 * @createTime 2017-06-05
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(83)
public class QuestDeviationAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(QuestDeviationAggregator.class);

    public static final String QUERY_QUEST = "select * from `{{table}}` order by score desc";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table quest_deviation");

        LOG.info("开始统计项目 ID {} 客观题区分度 ...", projectId);
        List<Row> studentScoreList = projectDao.query("select * from score_project order by score desc");

        List<Row> studentInfoList = projectDao.query("select * from student");

        ThreadPoolExecutor pool = Executors.newBlockingThreadPoolExecutor(20, 20, 1);
        List<ExamQuest> quests = questService.queryQuests(projectId)
                .stream()
                .filter(quest -> quest.isObjective())
                .collect(Collectors.toList());
        AsyncCounter counter = new AsyncCounter("正在统计客观题区分度", quests.size());

        quests.stream()
                .forEach(quest ->
                        pool.submit(() ->
                                aggrQuestDistinction(projectId, projectDao, quest, studentScoreList, studentInfoList, counter, pool)));

        LOG.info("项目 ID {} 统计题目区分度完成 ...", projectId);
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);
    }

    private void aggrQuestDistinction(String projectId, DAO projectDao, ExamQuest quest, List<Row> studentTotalScoreList, List<Row> studentInfoList, AsyncCounter counter, ThreadPoolExecutor pool) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String questId = quest.getId();
        String table = "score_" + questId;
        List<Row> questScoreRows = projectDao.query(QUERY_QUEST.replace("{{table}}", table));

        try {
            //处理Province维度区分度
            processProvinceData(quest, studentTotalScoreList, insertMap, questScoreRows);

            //处理School维度区分度
            schoolService.listSchool(projectId)
                    .forEach(school -> processSchoolData(quest, school, studentTotalScoreList, insertMap, questScoreRows, studentInfoList));

            //处理Class维度区分度
            classService.listClasses(projectId)
                    .forEach(clazz -> processClassData(quest, clazz, studentTotalScoreList, insertMap, questScoreRows, studentInfoList));
        } finally {

            projectDao.insert(insertMap, "quest_deviation");
            counter.count();
        }


    }

    private void processClassData(ExamQuest quest, ProjectClass clazz, List<Row> studentTotalScoreList,
                                  List<Map<String, Object>> insertMap, List<Row> questScoreRows, List<Row> studentInfoList) {
        String classId = clazz.getId();
        //班级学生列表
        List<String> classStudentList = studentInfoList.stream()
                .filter(row -> classId.equals(row.getString("class_id")))
                .map(row -> row.getString("id"))
                .collect(Collectors.toList());
        //班级学生总分Rows
        List<Row> classStudentTotalScoreRows = studentTotalScoreList
                .stream()
                .filter(row -> classStudentList.contains(row.getString("student_id")))
                .collect(Collectors.toList());

        int classCount = (int) Math.ceil(classStudentTotalScoreRows.size() * 0.27);

        //根据班级总分Rows 得到前27%的学生列表
        List<String> studentIdDesc = classStudentTotalScoreRows
                .stream()
                .limit(classCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());
        //根据班级总分Rows 得到后27%的学生列表
        List<String> studentIdAsc = classStudentTotalScoreRows.stream()
                .sorted((row1, row2) -> sored(row1, row2))
                .limit(classCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());

        //班级前27%的题目得分详情
        List<Row> studentQuestDescRows = questScoreRows.stream()
                .filter(row -> studentIdDesc.contains(row.getString("student_id")))
                .collect(Collectors.toList());
        //班级后27%的题目得分详情
        List<Row> studentQuestAscRows = questScoreRows.stream()
                .filter(row -> studentIdAsc.contains(row.getString("student_id")))
                .collect(Collectors.toList());

        double value = calculateDistinctionValue(quest, studentQuestDescRows, studentQuestAscRows);
        insertMap.add(createMap(quest.getId(), Range.Class.name(), classId, value));
    }

    private void processSchoolData(ExamQuest quest, ProjectSchool school, List<Row> studentTotalScoreList,
                                   List<Map<String, Object>> insertMap, List<Row> questScoreRows, List<Row> studentInfoList) {
        String schoolId = school.getId();
        //学校学生列表
        List<String> schoolStudentList = studentInfoList.stream()
                .filter(row -> schoolId.equals(row.getString("school_id")))
                .map(row -> row.getString("id"))
                .collect(Collectors.toList());
        //学校学生总成绩
        List<Row> schoolStudentTotalScoreRows = studentTotalScoreList
                .stream()
                .filter(row -> schoolStudentList.contains(row.getString("student_id")))
                .collect(Collectors.toList());

        int schoolCount = (int) Math.ceil(schoolStudentTotalScoreRows.size() * 0.27);

        //学校学生成绩前27%的列表
        List<String> studentIdDesc = schoolStudentTotalScoreRows
                .stream()
                .limit(schoolCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());
        //学校学生成绩后27%的学生列表
        List<String> studentIdAsc = schoolStudentTotalScoreRows.stream()
                .sorted((row1, row2) -> sored(row1, row2))
                .limit(schoolCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());

        //学校学学生成绩前27%的学生成绩
        List<Row> studentQuestDescRows = questScoreRows.stream()
                .filter(row -> studentIdDesc.contains(row.getString("student_id")))
                .collect(Collectors.toList());
        //学校学学生成绩后27%的学生成绩
        List<Row> studentQuestAscRows = questScoreRows.stream()
                .filter(row -> studentIdAsc.contains(row.getString("student_id")))
                .collect(Collectors.toList());

        double value = calculateDistinctionValue(quest, studentQuestDescRows, studentQuestAscRows);
        insertMap.add(createMap(quest.getId(), Range.School.name(), schoolId, value));
    }


    private void processProvinceData(ExamQuest quest, List<Row> studentScoreList, List<Map<String, Object>> insertMap, List<Row> rows) {
        String questId = quest.getId();
        int provinceCount = (int) Math.ceil(studentScoreList.size() * 0.27);

        //省维度前27%的学生列表
        List<String> provinceStudentList = studentScoreList.stream()
                .limit(provinceCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());

        //省维度后27%的学生列表
        List<String> provinceStudentListAsc = studentScoreList.stream()
                .sorted((row1, row2) -> sored(row1, row2))
                .limit(provinceCount)
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());
        //省维度前27%的学生题目得分详情
        List<Row> provinceStudentDescRows = rows.stream()
                .filter(row -> provinceStudentList.contains(row.getString("student_id")))
                .collect(Collectors.toList());
        //省维度后27%的学生题目得分详情
        List<Row> provinceStudentAscRows = rows.stream()
                .filter(row -> provinceStudentListAsc.contains(row.getString("student_id")))
                .collect(Collectors.toList());

        double value = calculateDistinctionValue(quest, provinceStudentDescRows, provinceStudentAscRows);
        insertMap.add(createMap(questId, Range.Province.name(), "430000", value));
    }


    //计算区分度
    private double calculateDistinctionValue(ExamQuest quest, List<Row> desc, List<Row> asc) {
        return (calculateAverage(desc) - calculateAverage(asc)) / quest.getFullScore();
    }

    //计算平均值
    private double calculateAverage(List<Row> rows) {
        return rows.stream()
                .mapToDouble(row -> row.getDouble("score", 0))
                .average().getAsDouble();
    }

    //升序排列
    private int sored(Row row1, Row row2) {
        return (row1.getDouble("score", 0) - row2.getDouble("score", 0) > 0) ? 1 : -1;
    }

    //创建一个Map用于插入到数据库
    private Map<String, Object> createMap(String questId, String rangeType, String rangeId, double value) {
        Map<String, Object> map = new HashMap<>();
        map.put("quest_id", questId);
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("value", value);
        return map;
    }
}
