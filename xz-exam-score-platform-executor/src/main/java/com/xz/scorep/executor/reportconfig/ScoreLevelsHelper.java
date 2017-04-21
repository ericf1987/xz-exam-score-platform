package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: luckylo
 * Date : 2017-04-17
 */
public class ScoreLevelsHelper {

    private ScoreLevelsHelper() {
    }

    public static double failScore(String subjectId, JSONObject jsonObject, String scoreLevelConfig, double fullScore) {
        return score(jsonObject, "Fail", subjectId, fullScore, scoreLevelConfig);
    }

    public static double passScore(String subjectId, JSONObject jsonObject, String scoreLevelConfig, double fullScore) {
        return score(jsonObject, "Pass", subjectId, fullScore, scoreLevelConfig);
    }

    public static double goodScore(String subjectId, JSONObject jsonObject, String scoreLevelConfig, double fullScore) {
        return score(jsonObject, "Good", subjectId, fullScore, scoreLevelConfig);
    }

    public static double excellentScore(String subjectId, JSONObject jsonObject, String scoreLevelConfig, double fullScore) {
        return score(jsonObject, "Excellent", subjectId, fullScore, scoreLevelConfig);
    }

    public static double score(JSONObject jsonObject, String type, String subjectId, double fullScore, String scoreLevelConfig) {

        if (scoreLevelConfig.equals("score")) {
            JSONObject subjectJson = jsonObject.getJSONObject(subjectId);
            return subjectJson.getDouble(type);
        } else {
            return fullScore * jsonObject.getDouble(type);
        }
    }

    public static Map<String, Double> getScoreLevels(String subjectId, String scoreLevelConfig, JSONObject scoreLevels) {
        Map<String, Double> result = new HashMap<>();
        if (scoreLevelConfig.equals("score")) {
            for (Map.Entry<String, Object> entry : scoreLevels.entrySet()) {
                if (entry.getKey().equals(subjectId)) {
                    JSONObject value = (JSONObject) entry.getValue();
                    value.forEach((key, val) -> result.put(key, Double.parseDouble(val.toString())));
                    return result;
                }
            }
        } else {
            result.put("Excellent", scoreLevels.getDouble("Excellent"));
            result.put("Good", scoreLevels.getDouble("Good"));
            result.put("Pass", scoreLevels.getDouble("Pass"));
            result.put("Fail", scoreLevels.getDouble("Fail"));
        }

        return result;
    }


}
