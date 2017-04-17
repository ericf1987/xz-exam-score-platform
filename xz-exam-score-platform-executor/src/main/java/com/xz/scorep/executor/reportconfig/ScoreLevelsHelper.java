package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSONObject;

/**
 * Author: luckylo
 * Date : 2017-04-17
 */
public class ScoreLevelsHelper {

    public static double failScore(String subjectId, JSONObject jsonObject, double fullScore) {
        return score(jsonObject, "Fail", subjectId, fullScore);
    }

    public static double passScore(String subjectId, JSONObject jsonObject, double fullScore) {
        return score(jsonObject, "Pass", subjectId, fullScore);
    }

    public static double goodScore(String subjectId, JSONObject jsonObject, double fullScore) {
        return score(jsonObject, "Good", subjectId, fullScore);
    }

    public static double excellentScore(String subjectId, JSONObject jsonObject, double fullScore) {
        return score(jsonObject, "Excellent", subjectId, fullScore);
    }

    public static double score(JSONObject jsonObject, String type, String subjectId, double fullScore) {
        if (jsonObject.getString("scoreLevelConfig").equals("score")) {
            JSONObject subjectJson = jsonObject.getJSONObject(subjectId);
            return subjectJson.getDouble(type);
        }
        if (jsonObject.getString("scoreLevelConfig").equals("rate")) {
            return fullScore * jsonObject.getDouble(type);
        }
        return 0;
    }

}
