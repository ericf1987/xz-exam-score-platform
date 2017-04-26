package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.util.StringUtil;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.importproject.ImportProjectService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.xz.scorep.executor.importproject.ImportProjectService.PROJECT_ID_KEY;

/**
 * (description)
 * created at 2017/1/24
 *
 * @author yidin
 */
public class ReportConfigParser implements ImportProjectService.ResultParser<ReportConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(ReportConfigParser.class);

    @Override
    public ReportConfig parse(Context context, Result result) {
        ReportConfig reportConfig = new ReportConfig();
        String projectId = context.getString(PROJECT_ID_KEY);
        reportConfig.setProjectId(projectId);

        String scoreLevelConfig = result.getString("scoreLevelConfig");
        if (!StringUtil.isEmpty(scoreLevelConfig)) {
            reportConfig.setScoreLevelConfig(scoreLevelConfig);
        }

        JSONObject scoreLevels = result.get("scoreLevels");
        if (scoreLevels != null) {
            Map<String, Object> newMap = new HashMap<>();

            if ("".equals(scoreLevelConfig) || scoreLevelConfig.equals("rate")) {
                scoreLevels.forEach((key, value) -> {
                    if (!StringUtil.isEmpty(value)) {
                        newMap.put(StringUtil.capitalize(key), value);//处理空串情况 key 改为首字母大写
                    }
                });
                //补充缺失的配置
                fillAbsentScoreLevels(newMap);
            } else {
                for (Map.Entry<String, Object> entry : scoreLevels.entrySet()) {
                    Map<String, Object> map = new HashMap<>();
                    String entryKey = entry.getKey();
                    JSONObject entryValue = (JSONObject) entry.getValue();
                    entryValue.forEach((key, value) -> newMap.put(StringUtil.capitalize(key), value));// key 改为首字母大写
                    newMap.put(entryKey, map);
                }
            }
            if (newMap.size() != 0) {
                reportConfig.setScoreLevels(JSON.toJSONString(newMap));
            }
        }


        String splitUnionSubject = result.getString("splitUnionSubject");
        if (splitUnionSubject != null) {
            reportConfig.setSeparateCategorySubjects(splitUnionSubject);
        }

        String topStudentRatio = result.getString("topStudentRatio");
        if (!StringUtil.isEmpty(topStudentRatio)) {
            reportConfig.setTopStudentRate(Double.parseDouble(topStudentRatio));
        }

        JSONObject rankLevelSettings = result.get("rankLevel");
        if (rankLevelSettings != null) {

            JSONObject rankLevelMap = rankLevelSettings.getJSONObject("standard");
            if (rankLevelMap != null) {
                reportConfig.setRankLevels(parseRankLevelMap(rankLevelMap));
            }

            JSONArray displayOptions = rankLevelSettings.getJSONArray("displayOptions");
            if (displayOptions != null) {
                reportConfig.setRankLevelCombines(displayOptions.toJSONString());
            }
        }

        //总分、单科分数步长
        JSONObject scoreStep = result.get("scoreStep");
        if (scoreStep != null) {
            Double subjectStep = scoreStep.getDouble("subject");
            if (subjectStep != null) {
                reportConfig.setSubjectSegment(subjectStep);
            }

            Double totalStep = scoreStep.getDouble("total");
            if (totalStep != null) {
                reportConfig.setTotalSegment(totalStep);
            }
        }

        String removeAbsentStudent = result.getString("removeAbsentStudent");
        if (!StringUtil.isEmpty(removeAbsentStudent)) {
            reportConfig.setRemoveAbsentStudent(removeAbsentStudent);
        }

        String removeCheatStudent = result.getString("removeCheatStudent");
        if (!StringUtil.isEmpty(removeCheatStudent)) {
            reportConfig.setRemoveCheatStudent(removeCheatStudent);
        }

        String removeZeroScores = result.getString("removeZeroScores");
        if (!StringUtil.isEmpty(removeZeroScores)) {
            reportConfig.setRemoveZeroScores(removeZeroScores);
        }

        String almostPassOffset = result.getString("almostPassOffset");
        if (!StringUtils.isBlank(almostPassOffset)) {
            try {
                reportConfig.setAlmostPassOffset(Double.parseDouble(almostPassOffset));
            } catch (NumberFormatException e) {
                LOG.info("项目ID:{}报表 almostPassOffset{} 配置有误....", projectId, almostPassOffset);
                reportConfig.setAlmostPassOffset(0);
            }
        }

        String fillAlmostPass = result.getString("fillAlmostPass");
        if (!StringUtil.isEmpty(fillAlmostPass)) {
            reportConfig.setFillAlmostPass(fillAlmostPass);
        }

        String highScoreRatio = result.getString("highScoreRatio");
        if (!StringUtil.isEmpty(highScoreRatio)) {
            reportConfig.setHighScoreRate(Double.parseDouble(highScoreRatio));
        }

        return reportConfig;
    }

    private void fillAbsentScoreLevels(Map<String, Object> newMap) {
        if (newMap.get("Excellent") == null) {
            newMap.put("Excellent", "0.9");
        }

        if (newMap.get("Good") == null) {
            newMap.put("Good", "0.8");
        }

        if (newMap.get("Pass") == null) {
            newMap.put("Pass", "0.6");
        }

        if (newMap.get("Fail") == null) {
            newMap.put("Fail", "0.0");
        }
    }

    /* 输入参数格式：{ "A":"40","B":"25","C":"23","D":"7","E":"4","F":"1" } */
    private String parseRankLevelMap(JSONObject rankLevelMap) {
        Map<String, Double> result = new HashMap<>();

        rankLevelMap.forEach((rank, rate) ->
                result.put(rank, Double.parseDouble(String.valueOf(rate)) / 100.0));

        return JSON.toJSONString(result);
    }
}
