package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreLevelMap;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class ScoreLevelMapQuery {
    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    SubjectService subjectService;

    static final Logger LOG = LoggerFactory.getLogger(ScoreLevelMapQuery.class);

    public static final String QUERY_DATA = "select * from scorelevelmap";

    public List<ScoreLevelMap> queryObj(String projectId) {

        LOG.info("开始查询 score_level_map 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<Row> rows = projectDao.query(QUERY_DATA);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<ScoreLevelMap> scoreLevelMaps = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            doProcess(projectId, rows, projectSchools, projectClasses, scoreLevelMaps, subjectId);
        }

        doProcess(projectId, rows, projectSchools, projectClasses, scoreLevelMaps, null);

        LOG.info("查询完成 score_level_map 共 {} 条.....", scoreLevelMaps.size());

        return scoreLevelMaps;
    }

    private void doProcess(String projectId, List<Row> rows, List<ProjectSchool> projectSchools, List<ProjectClass> projectClasses, List<ScoreLevelMap> scoreLevelMaps, String subjectId) {
        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();

            List<Row> data = rows.stream().filter(
                    r -> schoolId.equals(r.getString("range_id")) && chooseProjectOrSubject(projectId, subjectId, r)
            ).collect(Collectors.toList());

            //学校单个科目数据
            scoreLevelMaps.add(packScoreLevelMap(projectId, subjectId, Range.SCHOOL, schoolId, data));
        }

        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();

            List<Row> data = rows.stream().filter(
                    r -> classId.equals(r.getString("range_id")) && chooseProjectOrSubject(projectId, subjectId, r)
            ).collect(Collectors.toList());

            //班级单个科目数据
            scoreLevelMaps.add(packScoreLevelMap(projectId, subjectId, Range.CLASS, classId, data));
        }
    }

    private boolean chooseProjectOrSubject(String projectId, String subjectId, Row r) {

        return StringUtils.isEmpty(subjectId) ?
                projectId.equals(r.getString("target_id")) : subjectId.equals(r.getString("target_id"));

    }

    private ScoreLevelMap packScoreLevelMap(String projectId, String subjectId, String rangeName, String rangeId, List<Row> data) {

        ScoreLevelMap scoreLevelMapObj = new ScoreLevelMap();
        scoreLevelMapObj.setProject(projectId);
        scoreLevelMapObj.setMd5(MD5.digest(UUID.randomUUID().toString()));

        Range range = new Range();
        range.setId(rangeId);
        range.setName(rangeName);

        Target target = new Target();
        if(StringUtils.isEmpty(subjectId)){
            target.setId(projectId);
            target.setName(Target.PROJECT);
        }else{
            target.setId(subjectId);
            target.setName(Target.SUBJECT);
        }

        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        for (Row row : data) {
            String score_level = row.getString("score_level");
            Map<String ,Object> map = new HashMap<>();
            map.put("count", row.get("student_count"));
            map.put("rate", row.get("student_rate"));
            if("FAIL".equals(score_level))
                map.put("scoreLevel", Keys.ScoreLevel.Fail.name());
            else if ("GOOD".equals(score_level))
                map.put("scoreLevel", Keys.ScoreLevel.Good.name());
            else if("PASS".equals(score_level))
                map.put("scoreLevel", Keys.ScoreLevel.Pass.name());
            else if("XLNT".equals(score_level))
                map.put("scoreLevel", Keys.ScoreLevel.Excellent.name());

            scoreLevels.add(map);
        }

        scoreLevelMapObj.setRange(range);
        scoreLevelMapObj.setTarget(target);
        scoreLevelMapObj.getScoreLevels().addAll(scoreLevels);

        return scoreLevelMapObj;
    }
}
