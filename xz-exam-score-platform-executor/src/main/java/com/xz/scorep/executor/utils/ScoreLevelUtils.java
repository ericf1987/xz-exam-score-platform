package com.xz.scorep.executor.utils;

import com.xz.ajiaedu.common.report.Keys;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * @author by fengye on 2017/5/15.
 */
public class ScoreLevelUtils {

    public static String calculateScoreLevel(double score, Map<String, Double> scoreLevels) {
        if (score >= MapUtils.getDouble(scoreLevels, Keys.ScoreLevel.Excellent.name())) {
            return Keys.ScoreLevel.Excellent.name();
        } else if (score >= MapUtils.getDouble(scoreLevels, Keys.ScoreLevel.Good.name())) {
            return Keys.ScoreLevel.Good.name();
        } else if (score >= MapUtils.getDouble(scoreLevels, Keys.ScoreLevel.Pass.name())) {
            return Keys.ScoreLevel.Pass.name();
        } else {
            return Keys.ScoreLevel.Fail.name();
        }
    }
}
