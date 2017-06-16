package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggritems.FullScoreQuery;
import com.xz.scorep.executor.aggritems.ScoreQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.ScoreRate;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.reportconfig.ScoreLevelsHelper;
import com.xz.scorep.executor.utils.DoubleUtils;
import com.xz.scorep.executor.utils.ScoreLevelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 科目/项目得分率统计
 *
 * @author by fengye on 2017/5/7.
 */

@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggregateOrder(53)
@Component
public class ScoreRateAggregator extends Aggregator {

    public static final String SUBJECT_TABLE_NAME = "score_rate_{{subjectId}}";

    public static final String PROJECT_TABLE_NAME = "score_rate_project";

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    @Autowired
    ScoreQuery scoreQuery;

    @Autowired
    FullScoreQuery fullScoreQuery;

    @Autowired
    ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        projectDao.execute("truncate table score_rate_project");
        //处理项目数据
        processProjectData(projectId, projectDao);

        //处理单科数据
        processSubjectData(projectId, projectDao, examSubjects);

    }


    private void processProjectData(String projectId, DAO projectDao) {

        //所有学生考试的全科总分
        List<Row> rows = scoreQuery.listStudentScore(projectId, Range.PROVINCE_RANGE, Target.project(projectId));
        //考试项目满分
        Double fullScore = fullScoreQuery.getFullScore(projectId, Target.project(projectId));

        Map<String, Double> scoreLevelMap = getScoreLevelMap(projectId, "000");

        //生成得分率记录
        List<ScoreRate> scoreRates = processScoreRate(rows, Target.project(projectId), fullScore, scoreLevelMap);
        //写入数据库
        projectDao.insert(scoreRates, PROJECT_TABLE_NAME);
    }

    private void processSubjectData(String projectId, DAO projectDao, List<ExamSubject> examSubjects) {
        for (ExamSubject es : examSubjects) {
            String subjectId = es.getId();
            String sql = "truncate table score_rate_{{subjectId}}";
            projectDao.execute(sql.replace("{{subjectId}}", subjectId));

            Target target = Target.subject(subjectId);
            List<Row> rows = scoreQuery.listStudentScore(projectId, Range.PROVINCE_RANGE, target);
            Double fullScore = es.getFullScore();
            Map<String, Double> scoreLevelMap = getScoreLevelMap(projectId, subjectId);
            List<ScoreRate> scoreRates = processScoreRate(rows, target, fullScore, scoreLevelMap);
            projectDao.insert(scoreRates, SUBJECT_TABLE_NAME.replace("{{subjectId}}", subjectId));
        }
    }

    public Map<String, Double> getScoreLevelMap(String projectId, String subjectId) {
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        String scoreLevelConfig = reportConfig.getScoreLevelConfig();
        JSONObject scoreLevels = JSONObject.parseObject(reportConfig.getScoreLevels());
        return ScoreLevelsHelper.getScoreLevels(subjectId, scoreLevelConfig, scoreLevels);
    }

    /**
     * 处理并生成得分率列表
     *
     * @param rows          分数记录
     * @param target        目标
     * @param fullScore     target对应满分
     * @param scoreLevelMap 分数等级
     * @return 返回结果
     */
    private List<ScoreRate> processScoreRate(List<Row> rows, Target target, Double fullScore, Map<String, Double> scoreLevelMap) {
        List<ScoreRate> scoreRates = new ArrayList<>();
        for (Row r : rows) {
            double score = r.getDouble(getScoreAlias(target), 0);
            double rate = DoubleUtils.round(score / fullScore, true);
            String scoreLevel = ScoreLevelUtils.calculateScoreLevel(rate, scoreLevelMap);
            String student_id = r.getString("student_id");
            scoreRates.add(new ScoreRate(student_id, Range.STUDENT, student_id, target.getId().toString(), target.getType(), scoreLevel, rate));
        }
        return scoreRates;
    }

    private String getScoreAlias(Target target) {
        return target.match(Target.PROJECT) ? "score_000" : "score_" + target.getId().toString();
    }

}
