package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * (description)
 * created at 2017/2/28
 *
 * @author yidin
 */
public class ScoreDetailServiceTest extends BaseTest {

    @Autowired
    private ScoreDetailService scoreDetailService;

    @Test
    public void getStudentSubjectScoreDetail() throws Exception {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        String subjectId = "009";
        String studentId = "f2ee47f0-1959-4f65-bb30-a7e3827189fe";

        Map<String, Double> scores = scoreDetailService.getStudentSubjectScoreDetail(projectId, studentId, subjectId);
        List<String> questIds = new ArrayList<>(scores.keySet());
        Collections.sort(questIds);

        for (String questId : questIds) {
            System.out.println(questId + " : " + scores.get(questId));
        }
    }

}