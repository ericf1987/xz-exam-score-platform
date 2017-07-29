package com.xz.scorep.executor.utils;

import com.xz.scorep.executor.reportconfig.ReportConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表配置转换(缺考,作弊,0分)
 *
 * @author luckylo
 * @createTime 2017-07-29.
 */
public class ReportConfigUtils {

    private static final List<String> DEFAULT = new ArrayList<>();

    static {
        DEFAULT.add(ScoreType.LOST.getName());
        DEFAULT.add(ScoreType.ABSENT.getName());
        DEFAULT.add(ScoreType.CHEAT.getName());
        DEFAULT.add(ScoreType.ZERO.getName());
        DEFAULT.add(ScoreType.PAPER.getName());
    }

    public static List<String> convertReportConfig(ReportConfig reportConfig) {
        List<String> list = new ArrayList<>(DEFAULT);
        list.remove(ScoreType.LOST.getName());

        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            list.remove(ScoreType.ABSENT.getName());
        }

        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())) {
            list.remove(ScoreType.CHEAT.getName());
        }

        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            list.remove(ScoreType.ZERO.getName());
        }

        return list.stream().map(str -> "'" + str + "'").collect(Collectors.toList());
    }

}
