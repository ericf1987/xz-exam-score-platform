package com.xz.scorep.executor.utils;

import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.bean.ScoreSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class ScoreSegmentUtils {

    public static List<ScoreSegment> getScoreSegments(double fullScore, Keys.Target target) {
        List<ScoreSegment> result = new ArrayList<>();
        int step = target == Keys.Target.Project ? 50 : 10;
        double counter = 0;

        while (counter < fullScore) {
            result.add(new ScoreSegment(counter, Math.min(counter + step, fullScore)));
            counter += step;
        }

        return result;
    }
}
