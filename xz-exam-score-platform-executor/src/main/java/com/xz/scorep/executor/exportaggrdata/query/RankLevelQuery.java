package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.RankLevel;
import com.xz.scorep.executor.project.StudentService;
import com.xz.scorep.executor.project.SubjectService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排名信息查询
 *
 * @author by fengye on 2017/7/20.
 */
@Component
public class RankLevelQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SubjectService subjectService;

    @Autowired
    StudentService studentService;

    static final Logger LOG = LoggerFactory.getLogger(RankLevelQuery.class);

    public static final String CLASS_DATA_QUERY = "select * from rank_level_class";

    public static final String SCHOOL_DATA_QUERY = "select * from rank_level_school";

    public static final String PROVINCE_DATA_QUERY = "select * from rank_level_province";

    public static final String PROJECT_DATA_QUERY = "select * from rank_level_project";

    public List<RankLevel> queryObj(String projectId) {

        LOG.info("开始查询 rank_level 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        //按科目查询班级 学校 总体的学生排名情况
        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        examSubjects.removeIf(s -> "000".equals(s.getId()));

        List<String> studentIds = studentService.listStudents(projectId);

        List<Row> class_rank_list = projectDao.query(CLASS_DATA_QUERY);
        List<Row> school_rank_list = projectDao.query(SCHOOL_DATA_QUERY);
        List<Row> province_rank_list = projectDao.query(PROVINCE_DATA_QUERY);
        List<Row> project_rank_list = projectDao.query(PROJECT_DATA_QUERY);

        List<RankLevel> rankLevelList = studentIds.parallelStream()
                .map(studentId -> packOneStudentRankLevel(studentId, examSubjects, projectId,
                        class_rank_list, school_rank_list, province_rank_list, project_rank_list))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());

        LOG.info("查询完成 rank_level 共 {} 条.....", rankLevelList.size());

        return rankLevelList;

    }

    private List<RankLevel> packOneStudentRankLevel(String studentId, List<ExamSubject> examSubjects, String projectId,
                                                    List<Row> class_rank_list, List<Row> school_rank_list, List<Row> province_rank_list,
                                                    List<Row> project_rank_list) {
        List<RankLevel> rankLevelList = new ArrayList<>();
        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            RankLevel rankLevelObj = new RankLevel();

            //单科的等级排名数据
            Map<String, Object> rankLevelMap = getRankLevel(class_rank_list, school_rank_list, province_rank_list, studentId, subjectId);

            packOneRankLevelObj(projectId, subjectId, rankLevelObj, studentId, rankLevelMap);

            rankLevelList.add(rankLevelObj);
        }

        RankLevel rankLevelObj = new RankLevel();

        //全科的等级排名数据
        Map<String, Object> projectRankLevel = getProjectRankLevel(project_rank_list, studentId);
        packOneRankLevelObj(projectId, null, rankLevelObj, studentId, projectRankLevel);
        rankLevelList.add(rankLevelObj);

        return rankLevelList;
    }

    private Map<String, Object> getProjectRankLevel(List<Row> project_rank_list, String studentId) {

        Optional<String> optional_p = project_rank_list.stream().filter(
                p -> studentId.equals(p.getString("student_id")) && Range.PROVINCE.equals(p.getString("range_type"))
        ).map(p -> p.getString("rank_level")).findFirst();

        Optional<String> optional_s = project_rank_list.stream().filter(
                s -> studentId.equals(s.getString("student_id")) && Range.SCHOOL.equals(s.getString("range_type"))
        ).map(s -> s.getString("rank_level")).findFirst();

        Optional<String> optional_c = project_rank_list.stream().filter(
                c -> studentId.equals(c.getString("student_id")) && Range.CLASS.equals(c.getString("range_type"))
        ).map(c -> c.getString("rank_level")).findFirst();

        Map<String, Object> m = new HashMap<>();
        m.put("province", optional_p.isPresent() ? optional_p.get() : "");
        m.put("school", optional_s.isPresent() ? optional_s.get() : "");
        m.put("class", optional_c.isPresent() ? optional_c.get() : "");

        return m;
    }

    private Map<String, Object> getRankLevel(List<Row> class_rank_list, List<Row> school_rank_list, List<Row> province_rank_list, String studentId, String subjectId) {
        Map<String, Object> m = new HashMap<>();

        Optional<String> optional_c = class_rank_list.stream().filter(
                s -> studentId.equals(s.getString("student_id")) && subjectId.equals(s.getString("subject_id"))
        ).map(s -> s.getString("rank_level")).findFirst();

        Optional<String> optional_s = school_rank_list.stream().filter(
                s -> studentId.equals(s.getString("student_id")) && subjectId.equals(s.getString("subject_id"))
        ).map(s -> s.getString("rank_level")).findFirst();

        Optional<String> optional_p = province_rank_list.stream().filter(
                p -> studentId.equals(p.getString("student_id")) && subjectId.equals(p.getString("subject_id"))
        ).map(p -> p.getString("rank_level")).findFirst();

        m.put("class", optional_c.isPresent() ? optional_c.get() : "");
        m.put("school", optional_s.isPresent() ? optional_s.get() : "");
        m.put("province", optional_p.isPresent() ? optional_p.get() : "");

        return m;
    }

    private void packOneRankLevelObj(String projectId, String subjectId, RankLevel rankLevelObj, String student_id, Map<String, Object> rankLevel) {
        Target subjectTarget = new Target();
        if (StringUtils.isBlank(subjectId)) {
            subjectTarget.setId(projectId);
            subjectTarget.setName(Target.PROJECT);
        } else {
            subjectTarget.setId(subjectId);
            subjectTarget.setName(Target.SUBJECT);
        }

        rankLevelObj.setTarget(subjectTarget);
        rankLevelObj.setStudent(student_id);
        rankLevelObj.getRankLevel().putAll(rankLevel);
        rankLevelObj.setProject(projectId);
        rankLevelObj.setMd5(MD5.digest(UUID.randomUUID().toString()));
    }

}
