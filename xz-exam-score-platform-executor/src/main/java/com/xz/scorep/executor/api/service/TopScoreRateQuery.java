package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.utils.SqlUtils.renderGroupType;
import static com.xz.scorep.executor.utils.SqlUtils.replaceRangeId;

/**
 * 得分率较高/较低的题目，用于快报
 *
 * @author by fengye on 2017/7/5.
 */
@Component
public class TopScoreRateQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    QuestService questService;

    public static final String SCORE_UNION = "select student_id, score, '{{quest_id}}' questId, '{{quest_no}}' questNo, '{{full_score}}' fullScore from score_{{quest_id}} ";

    public static final String SCORE_DETAIL = "SELECT {{more_group_type}} questId, questNo, stu.{{range_id}} range_id, scores.fullScore FROM student stu,\n" +
            "({{union_table}}) scores\n" +
            "WHERE stu.id = scores.student_id\n" +
            "GROUP BY questId, questNo, fullScore, range_id";

    public List<Row> getTop(List<Row> combinedRows, int count, boolean asc) {

        //按照班级得分率排序

        return combinedRows.stream().sorted((Row r1, Row r2) -> {
            Double d1 = r1.getDouble("rate", 0);
            Double d2 = r2.getDouble("rate", 0);
            return asc ? d1.compareTo(d2) : d2.compareTo(d1);
        }).limit(count).collect(Collectors.toList());
    }

    public List<Row> combineByRange(List<Row> classData, List<Row> schoolData) {
        //获取班级得分率前五的题目ID
        classData.stream().forEach(c -> {
            String questId = c.getString("questId");
            schoolData.stream().filter(s -> questId.equals(s.getString("questId"))).forEach(s -> {
                c.put("parent_range_id", s.getString("range_id"));
                c.put("parent_avg", DoubleUtils.round(s.getDouble("avg", 0)));
                c.put("parent_rate", s.getDouble("rate", 0));
            });
        });
        return classData;
    }

    /**
     * 获取特定维度的试题的得分和得分率情况
     *
     * @param projectId  项目ID
     * @param subjectId  科目ID
     * @param rangeName  维度名称
     * @param rangeId    维度ID
     * @param asc        排序方式
     * @param groupTypes 分组类型
     * @return 返回结果
     */
    public List<Row> getScoreRate(String projectId, String subjectId, String rangeName, String rangeId,
                                  List<ExamQuest> examQuests, boolean asc, String... groupTypes) {
        List<Row> var7 = getQuestScoresGroup(projectId, subjectId, rangeName, examQuests, groupTypes);

        List<Row> var8 = var7.stream().filter(r -> rangeId.equals(r.getString("range_id"))).collect(Collectors.toList());

        var8.forEach(row -> {
                    row.put("avg", DoubleUtils.round(row.getDouble("avg", 0)));
                    row.put("rate", DoubleUtils.round(row.getDouble("avg", 0) / Double.parseDouble(row.getString("fullScore")), true));
                }
        );

        Collections.sort(var8, (Row r1, Row r2) -> {
            Double d1 = r1.getDouble("rate", 0);
            Double d2 = r2.getDouble("rate", 0);
            return asc ? d1.compareTo(d2) : d2.compareTo(d1);
        });

        return var8;
    }

    /**
     * 分组计算科目下所有试题的得分
     *
     * @param projectId  项目ID
     * @param subjectId  科目ID
     * @param rangeName  维度名称
     * @param examQuests 试题列表
     * @param groupTypes 分组查询参数
     * @return 返回结果
     */
    public List<Row> getQuestScoresGroup(String projectId, String subjectId, String rangeName, List<ExamQuest> examQuests, String... groupTypes) {

        List<String> questNos = examQuests.stream().map(ExamQuest::getQuestNo).collect(Collectors.toList());

        String sql = renderGroupType(
                replaceRangeId(rangeName, SCORE_DETAIL.replace("{{union_table}}", unionScore(examQuests))), groupTypes
        );

        DAO projectDao = daoFactory.getProjectDao(projectId);

        String cacheKey = "score_quests:" + projectId + ":" + subjectId + ":" + rangeName + ":" + Arrays.toString(groupTypes) + ":" + questNos.toString();

        return cacheFactory.getProjectCache(projectId).get(cacheKey, () -> new ArrayList<>(projectDao.query(sql)));
    }

    //关联试题列表对应的所有得分记录
    public String unionScore(List<ExamQuest> examQuests) {
        return StringUtil.joinPaths(" UNION ALL ",
                examQuests.stream().map(examQuest -> SCORE_UNION.replace("{{quest_id}}", examQuest.getId()).replace("{{quest_no}}", examQuest.getQuestNo()).replace("{{full_score}}", String.valueOf(examQuest.getFullScore()))).collect(Collectors.toList())
        );
    }
}
