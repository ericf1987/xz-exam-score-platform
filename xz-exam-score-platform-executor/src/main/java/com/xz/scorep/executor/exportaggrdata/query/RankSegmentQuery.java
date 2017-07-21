package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.RankSegment;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankSegmentQuery {

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

    static final Logger LOG = LoggerFactory.getLogger(RankSegmentQuery.class);

    public static final String QUERY_DATA = "select * from rank_segment";

    public List<RankSegment> queryObj(String projectId){

        LOG.info("开始查询 rank_level 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<Row> rows = projectDao.query(QUERY_DATA);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<RankSegment> result = new ArrayList<>();
        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            doProcess(projectId, rows, projectSchools, projectClasses, result, subjectId);

        }

        doProcess(projectId, rows, projectSchools, projectClasses, result, "000");

        LOG.info("查询完成 rankSegment 共 {} 条.....", result.size());

        return result;
    }

    private void doProcess(String projectId, List<Row> rows, List<ProjectSchool> projectSchools, List<ProjectClass> projectClasses, List<RankSegment> result, String subjectId) {
        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();
            List<Row> data = rows.stream().filter(
                    r -> schoolId.equals(r.getString("range_id")) && subjectId.equals(r.getString("subject_id"))
            ).collect(Collectors.toList());

            //将一个学校的一个科目对应的分段数据封装成一个rankSegment对象
            result.add(packRankSegments(projectId, subjectId, schoolId, Range.SCHOOL, data));
        }

        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();
            List<Row> data = rows.stream().filter(
                    r -> classId.equals(r.getString("range_id")) && subjectId.equals(r.getString("subject_id"))
            ).collect(Collectors.toList());

            //将一个班级的一个科目对应的分段数据封装成一个rankSegment对象
            result.add(packRankSegments(projectId, subjectId, classId, Range.CLASS, data));
        }
    }

    private RankSegment packRankSegments(String projectId, String subjectId, String rangeId, String rangeName, List<Row> rows) {

        Range range = new Range();
        range.setId(rangeId);
        range.setName(rangeName);

        Target target = new Target();
        if("000".equals(subjectId)){
            target.setId(projectId);
            target.setName(Target.PROJECT);
        }else{
            target.setId(subjectId);
            target.setName(Target.SUBJECT);
        }

        List<Map<String, Object>> rankSegments = rows.stream().map(
                r -> {
                    Map<String, Object> rankSegment = new HashMap<>();
                    rankSegment.put("rankPercent", r.get("rank_percent"));
                    rankSegment.put("rate", r.get("segment_rate"));
                    rankSegment.put("count", r.get("segment_count"));
                    return rankSegment;
                }
        ).collect(Collectors.toList());

        RankSegment rankSegment = new RankSegment();
        rankSegment.setProject(projectId);
        rankSegment.setMd5(MD5.digest(UUID.randomUUID().toString()));
        rankSegment.setRange(range);
        rankSegment.setTarget(target);
        rankSegment.getRankSegments().addAll(rankSegments);

        return rankSegment;
    }
}
