package com.xz.scorep.executor.utils;

import com.xz.ajiaedu.common.report.Keys;
import org.junit.Test;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class ScoreSegmentUtilsTest {

    @Test
    public void getScoreSegments() throws Exception {
        System.out.println(ScoreSegmentUtils.getScoreSegments(150, Keys.Target.Subject));
        System.out.println(ScoreSegmentUtils.getScoreSegments(900, Keys.Target.Project));
    }

}