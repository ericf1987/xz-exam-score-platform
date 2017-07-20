package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.RankLevelMap;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankLevelMapQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    ClassService classService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    SubjectService subjectService;

    public static final String SCHOOL_DATA_QUERY = "select * from rank_level_map_school";
    public static final String CLASS_DATA_QUERY = "select * from rank_level_map_class";
    public static final String PROJECT_DATA_QUERY = "select * from rank_level_map_project";

    public List<RankLevelMap> queryObj(String projectId){

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<Row> rank_level_map_school = projectDao.query(SCHOOL_DATA_QUERY);
        List<Row> rank_level_map_class = projectDao.query(CLASS_DATA_QUERY);
        List<Row> rank_level_map_project = projectDao.query(PROJECT_DATA_QUERY);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        examSubjects.removeIf(s -> "000".equals(s.getId()));

        List<RankLevelMap> result = new ArrayList<>();

        //学校数据
        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();

            List<Row> schoolDatas = rank_level_map_school.stream().filter(
                    s -> schoolId.equals(s.getString("school_id"))
            ).collect(Collectors.toList());

            //获取单个学校所有科目的数据列表
            result.addAll(packRankLevelMap(projectId, Range.SCHOOL, schoolId, schoolDatas, examSubjects));

            //获取单个学校整个项目的数据
            List<Row> projectData = rank_level_map_project.stream().filter(
                    s -> schoolId.equals(s.getString("range_id")) && Range.SCHOOL.equals(s.getString("range_type"))
            ).collect(Collectors.toList());
            result.add(packProjectRankLevelMap(projectId, Range.SCHOOL, schoolId, projectData));
        }

        //班级数据
        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();

            List<Row> classDatas = rank_level_map_class.stream().filter(
                    s -> classId.equals(s.getString("classId"))
            ).collect(Collectors.toList());

            //获取单个班级所有科目的数据列表
            result.addAll(packRankLevelMap(projectId, Range.CLASS, classId, classDatas, examSubjects));

            //获取单个班级整个项目的数据
            List<Row> projectData = rank_level_map_project.stream().filter(
                    s -> classId.equals(s.getString("range_id")) && Range.CLASS.equals(s.getString("range_type"))
            ).collect(Collectors.toList());
            result.add(packProjectRankLevelMap(projectId, Range.CLASS, classId, projectData));
        }

        return result;
    }

    private RankLevelMap packProjectRankLevelMap(String projectId, String rangeName, String schoolId, List<Row> projectData) {

        RankLevelMap rankLevelMapObj = new RankLevelMap();

        Range range = new Range();
        range.setName(rangeName);
        range.setId(schoolId);

        Target target = new Target();
        target.setName(Target.PROJECT);
        target.setId(projectId);

        List<Map<String, Object>> rankLevelMap = projectData.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("rankLevel", p.getString("rank_level"));
            m.put("count", p.get("cnt"));
            return m;
        }).collect(Collectors.toList());

        rankLevelMapObj.setRange(range);
        rankLevelMapObj.setTarget(target);
        rankLevelMapObj.setProject(projectId);
        rankLevelMapObj.setMd5(MD5.digest(UUID.randomUUID().toString()));
        rankLevelMapObj.getRankLevelMap().addAll(rankLevelMap);
        return rankLevelMapObj;
    }

    private List<RankLevelMap> packRankLevelMap(String projectId, String rangeName, String schoolId, List<Row> schoolDatas, List<ExamSubject> examSubjects) {

        List<RankLevelMap> rankLevelMapList = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            RankLevelMap rankLevelMapObj = new RankLevelMap();

            String subjectId = examSubject.getId();

            Range range = new Range();
            range.setName(rangeName);
            range.setId(schoolId);

            Target target = new Target();
            target.setName(Target.SUBJECT);
            target.setId(subjectId);

            List<Map<String, Object>> rankLevelMap = schoolDatas.stream().filter(
                    s -> subjectId.equals(s.getString("subject_Id"))
            ).map(s -> {
                Map<String, Object> m = new HashMap<>();
                m.put("rankLevel", s.getString("rank_level"));
                m.put("count", s.get("cnt"));
                return m;
            }).collect(Collectors.toList());

            rankLevelMapObj.setRange(range);
            rankLevelMapObj.setTarget(target);
            rankLevelMapObj.setProject(projectId);
            rankLevelMapObj.setMd5(MD5.digest(UUID.randomUUID().toString()));
            rankLevelMapObj.getRankLevelMap().addAll(rankLevelMap);

            rankLevelMapList.add(rankLevelMapObj);
        }

        return rankLevelMapList;

    }

}
