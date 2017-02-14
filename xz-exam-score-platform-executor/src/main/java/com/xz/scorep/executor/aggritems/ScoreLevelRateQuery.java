package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.report.Keys.Range;
import com.xz.ajiaedu.common.report.Keys.Target;
import com.xz.scorep.executor.bean.ScoreLevelRate;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScoreLevelRateQuery {

    public static final String SCHOOL_SLR =
            "select * from scorelevelmap where range_type=? and range_id=? and target_type=? and target_id=?";

    public static final String CLASS_SLRS = "select * \n" +
            "from \n" +
            "  scorelevelmap\n" +
            "where \n" +
            "  target_type= ? and \n" +
            "  target_id  = ? and \n" +
            "  range_type = ? and \n" +
            "  range_id   in (\n" +
            "    select id from class where school_id=?\n" +
            "  )";

    @Autowired
    private DAOFactory daoFactory;

    // 查询学校总分四率
    public List<ScoreLevelRate> getSchoolProjectSLR(String projectId, String schoolId) {
        return getSchoolSLR(projectId, schoolId, Target.Project, projectId);
    }

    // 查询班级总分四率
    public Map<String, List<ScoreLevelRate>> getClassProjectSLRs(String projectId, String schoolId) {

        return getClassSLRs(projectId, schoolId, Target.Project, projectId);
    }

    //////////////////////////////////////////////////////////////

    // 查询班级四率
    private Map<String, List<ScoreLevelRate>> getClassSLRs(
            String projectId, String schoolId, Target target, String targetId) {

        List<Row> rows = daoFactory.getProjectDao(projectId).query(
                CLASS_SLRS, target.name(), targetId, Range.Class.name(), schoolId);

        Map<String, List<ScoreLevelRate>> result = new HashMap<>();

        rows.forEach(row -> {
            String classId = row.getString("range_id");
            ScoreLevelRate rate = new ScoreLevelRate(row);

            if (!result.containsKey(classId)) {
                result.put(classId, new ArrayList<>());
            }

            result.get(classId).add(rate);
        });

        return result;
    }

    // 查询学校四率
    private List<ScoreLevelRate> getSchoolSLR(String projectId, String schoolId, Target target, String targetId) {

        List<Row> rows = daoFactory.getProjectDao(projectId).query(
                SCHOOL_SLR, Range.School.name(), schoolId, target.name(), targetId);

        return rows.stream().map(ScoreLevelRate::new).collect(Collectors.toList());
    }
}
