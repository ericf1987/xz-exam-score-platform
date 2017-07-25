package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreSegment;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.exportaggrdata.utils.AggrBeanUtils.setTarget;

/**
 * @author by fengye on 2017/7/25.
 */
@Component
public class ScoreSegmentsQuery {

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

    public static final String QUERY_DATA = "select * from score_segments";

    public List<ScoreSegment> queryObj(String projectId) {

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<Row> rows = projectDao.query(QUERY_DATA);

        return doProcess(projectId, examSubjects, projectSchools, projectClasses, rows);
    }

    private List<ScoreSegment> doProcess(String projectId, List<ExamSubject> examSubjects, List<ProjectSchool> projectSchools, List<ProjectClass> projectClasses, List<Row> rows) {
        Range provinceRange = Range.PROVINCE_RANGE;

        List<ScoreSegment> result = new ArrayList<>();


        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();

            filterAndPack(projectId, Range.SCHOOL, schoolId, examSubjects, rows, result);
            filterAndPack(projectId, Range.SCHOOL, schoolId, null, rows, result);
        }

        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();

            filterAndPack(projectId, Range.CLASS, classId, examSubjects, rows, result);
            filterAndPack(projectId, Range.CLASS, classId, null, rows, result);
        }

        filterAndPack(projectId, Range.PROVINCE, provinceRange.getId(), examSubjects, rows, result);
        filterAndPack(projectId, Range.PROVINCE, provinceRange.getId(), null, rows, result);

        return result;
    }

    private void filterAndPack(String projectId, String rangeName, String rangeId, List<ExamSubject> examSubjects, List<Row> rows, List<ScoreSegment> result) {

        if(null != examSubjects){
            examSubjects.forEach(s -> {
                String subjectId = s.getId();

                List<Row> matchRows = rows.stream().filter(
                        r -> rangeId.equals(r.getString("range_id")) && subjectId.equals(r.getString("target_id"))
                ).collect(Collectors.toList());

                result.add(packScoreSegment(projectId, rangeName, rangeId, subjectId, matchRows));
            });
        }else{
            List<Row> matchRows = rows.stream().filter(
                    r -> rangeId.equals(r.getString("range_id")) && projectId.equals(r.getString("target_id"))
            ).collect(Collectors.toList());

            result.add(packScoreSegment(projectId, rangeName, rangeId, null, matchRows));
        }
    }

    private ScoreSegment packScoreSegment(String projectId, String rangeName, String rangeId, String targetId, List<Row> matchRows) {

        Range range = new Range();
        range.setId(rangeId);
        range.setName(rangeName);

        Target target = new Target();
        setTarget(projectId, targetId, target);

        List<Map<String, Object>> scoreSegments = matchRows.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("segment", m.get("score_min"));
            map.put("count", m.get("student_count"));
            return map;
        }).collect(Collectors.toList());

        ScoreSegment scoreSegment = new ScoreSegment();
        scoreSegment.setRange(range);
        scoreSegment.setTarget(target);
        scoreSegment.setAggrObject(scoreSegment, projectId);
        scoreSegment.getScoreSegments().addAll(scoreSegments);

        return scoreSegment;
    }
}
