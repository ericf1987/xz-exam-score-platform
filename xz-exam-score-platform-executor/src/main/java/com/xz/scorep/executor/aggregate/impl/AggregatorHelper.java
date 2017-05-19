package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: luckylo
 * Date : 2017-04-21
 */
public class AggregatorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatorHelper.class);

    public static List<ExamSubject> getSubjects(AggregateParameter aggregateParameter, SubjectService subjectService) {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects;

        List<String> paramSubjects = aggregateParameter.getSubjects();
        LOG.info("传入的科目参数：" + paramSubjects);

        if (paramSubjects.isEmpty()) {
            subjects = subjectService.listSubjects(projectId);

        } else {
            subjects = paramSubjects
                    .stream()
                    .map(subjectId -> {
                        ExamSubject subject = subjectService.findSubject(projectId, subjectId);

                        if (subject == null) {
                            throw new IllegalStateException("科目 " + subjectId + " 没找到");
                        }

                        return subject;
                    })
                    .sorted()
                    .sorted(Comparator.comparingInt(a -> a.getId().length()))
                    .collect(Collectors.toList());
        }
        return subjects;
    }


    public static Map<String, Double> calculateRangeStdDeviation(List<Row> stdDeviationRows, Map<String, List<Row>> studentScores) {
        Map<String, Double> rangStdDeviation = new HashMap<>();
        stdDeviationRows.forEach(row -> {
            String rangId = row.getString("rangeId");
            double average = row.getDouble("average", 0);
            List<Row> rangScoreRows = studentScores.get(rangId);

            int studentCount = rangScoreRows.size();
            if (studentCount == 0) {
                LOG.info("rangId {}  对应人数为0", rangId);
                return;
            }

            double stdDeviation = calculateStdDeviation(average, studentCount, rangScoreRows);
            rangStdDeviation.put(rangId, stdDeviation);
        });

        return rangStdDeviation;
    }

    private static double calculateStdDeviation(double average, int studentCount, List<Row> rows) {
        double score = 0;
        for (Row row : rows) {
            double studentScore = row.getDouble("score", 0);
            score += (studentScore - average) * (studentScore - average);
        }
        return Math.sqrt(score / studentCount);
    }
}
