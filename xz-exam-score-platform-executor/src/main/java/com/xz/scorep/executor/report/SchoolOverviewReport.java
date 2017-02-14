package com.xz.scorep.executor.report;

import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.aggritems.MinMaxQuery;
import com.xz.scorep.executor.aggritems.ScoreLevelRateQuery;
import com.xz.scorep.executor.aggritems.StudentCountQuery;
import com.xz.scorep.executor.bean.MinMax;
import com.xz.scorep.executor.bean.ScoreLevelRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchoolOverviewReport extends AbstractReport {

    @Autowired
    private AverageQuery averageQuery;

    @Autowired
    private StudentCountQuery studentCountQuery;

    @Autowired
    private MinMaxQuery minMaxQuery;

    @Autowired
    private ScoreLevelRateQuery scoreLevelRateQuery;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId) {

        // 各科平均分
        Map<String, String> schoolSubjectAverages = averageQuery.getSchoolSubjectAverages(projectId, schoolId);

        // （学校和班级）总分人数
        int schoolStudentCount = studentCountQuery.getSchoolStudentCount(projectId, schoolId);
        Map<String, Integer> classStudentCounts = studentCountQuery.getClassStudentCount(projectId, schoolId);

        // （学校和班级）总分最低分、最高分
        MinMax schoolMinMax = minMaxQuery.getSchoolProjectMinMax(projectId, schoolId);
        Map<String, MinMax> classMinMax = minMaxQuery.getClassProjectMinMax(projectId, schoolId);

        // （学校和班级）总分平均分
        double schoolProjectAverage = averageQuery.getSchoolProjectAverage(projectId, schoolId);
        Map<String, Double> classProjectAverages = averageQuery.getClassProjectAverages(projectId, schoolId);

        // （学校和班级）总分四率
        List<ScoreLevelRate> schoolProjectSLR = scoreLevelRateQuery.getSchoolProjectSLR(projectId, schoolId);
        Map<String, List<ScoreLevelRate>> classProjectSLRs = scoreLevelRateQuery.getClassProjectSLRs(projectId, schoolId);

        // 返回结果
        HashMap<Object, Object> result = new HashMap<>();
        result.put("schoolSubjectAverages", schoolSubjectAverages);
        result.put("schoolStudentCount", schoolStudentCount);
        result.put("classStudentCounts", classStudentCounts);
        result.put("schoolMinMax", schoolMinMax);
        result.put("classMinMax", classMinMax);
        result.put("schoolProjectAverage", schoolProjectAverage);
        result.put("classProjectAverages", classProjectAverages);
        result.put("schoolProjectScoreLevelRate", schoolProjectSLR);
        result.put("classProjectScoreLevelRates", classProjectSLRs);
        return result;
    }
}
