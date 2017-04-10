package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.util.StringUtil;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.importproject.ImportProjectService;

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

    @Override
    public ReportConfig parse(Context context, Result result) {
        ReportConfig reportConfig = new ReportConfig();
        reportConfig.setProjectId(context.getString(PROJECT_ID_KEY));

        JSONObject scoreLevels = result.get("scoreLevels");
        if (scoreLevels != null) {
            Map<String, Object> newMap = new HashMap<>();
            scoreLevels.forEach((key, value) -> newMap.put(StringUtil.capitalize(key), value)); // key 改为首字母大写
            reportConfig.setScoreLevels(JSON.toJSONString(newMap));
        }

        String splitUnionSubject = result.getString("splitUnionSubject");
        if (splitUnionSubject != null) {
            reportConfig.setSeparateCategorySubjects(splitUnionSubject);
        }

        String topStudentRatio = result.getString("topStudentRatio");
        if (topStudentRatio != null) {
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

        // TODO: 2017-04-10   接口返回是否删除缺考学生

        // TODO: 2017-04-10   接口返回是否统计0分  属性 

        String highScoreRatio = result.getString("highScoreRatio");
        if (highScoreRatio != null) {
            reportConfig.setHighScoreRate(Double.parseDouble(highScoreRatio));
        }

        return reportConfig;
    }

    /* 输入参数格式：{ "A":"40","B":"25","C":"23","D":"7","E":"4","F":"1" } */
    private String parseRankLevelMap(JSONObject rankLevelMap) {
        Map<String, Double> result = new HashMap<>();

        rankLevelMap.forEach((rank, rate) ->
                result.put(rank, Double.parseDouble(String.valueOf(rate)) / 100.0));

        return JSON.toJSONString(result);
    }
}
