package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.SubjectRate;
import com.xz.scorep.executor.exportaggrdata.utils.QueryTaskThread;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author by fengye on 2017/7/21.
 */
@Component
public class SubjectRateQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    public static final String QUERY_DATA = "select score_rate.*, '{{subject_id}}' subject_Id from subject_rate_{{subject_id}} score_rate";

    static final Logger LOG = LoggerFactory.getLogger(SubjectRateQuery.class);

    public List<SubjectRate> queryObj(String projectId) {

        LOG.info("开始查询 subject_rate 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<QueryTaskThread> tasks = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            QueryTaskThread task = new QueryTaskThread(projectDao, SqlUtils.replaceSubjectId(QUERY_DATA, subjectId), Collections.emptyList());
            task.start();
            tasks.add(task);
        }

        //存放多个线程查询返回的结果
        List<Row> result = new ArrayList<>();
        for (QueryTaskThread task : tasks) {
            try {
                task.join();
                result.addAll(task.getResult());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<SubjectRate> subjectRates = doProcess(projectId, projectSchools, projectClasses, result, examSubjects);

        LOG.info("查询完成 subject_rate 共 {} 条.....", subjectRates.size());

        return subjectRates;
    }

    //根据学校和科目封装结果
    private List<SubjectRate> doProcess(String projectId, List<ProjectSchool> projectSchools,
                                        List<ProjectClass> projectClasses, List<Row> result, List<ExamSubject> examSubjects) {

        List<SubjectRate> list = new ArrayList<>();

        //学校数据
        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();

            list.add(packSubjectRate(projectId, schoolId, Range.SCHOOL, examSubjects, result));

        }

        //班级数据
        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();

            list.add(packSubjectRate(projectId, classId, Range.CLASS, examSubjects, result));
        }

        return list;
    }

    private Map<String, Object> getSubjectRateList(List<Row> result, String subjectId, String rangeId) {

        Optional<Map<String, Object>> first = result.stream().filter(
                r -> rangeId.equals(r.getString("range_id")) && subjectId.equals(r.getString("subject_id"))
        ).map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("subject", r.getString("subject_id"));
            m.put("rate", r.get("subject_rate"));
            return m;
        }).findFirst();

        if(first.isPresent()){
            return first.get();
        }

        return Collections.emptyMap();
    }

    private SubjectRate packSubjectRate(String projectId, String rangeId, String rangeName, List<ExamSubject> examSubjects, List<Row> result) {

        Range range = new Range();
        range.setId(rangeId);
        range.setName(rangeName);

        List<Map<String, Object>> subjectRateList = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            subjectRateList.add(getSubjectRateList(result, subjectId, rangeId));
        }

        SubjectRate subjectRate = new SubjectRate();
        subjectRate.setAggrObject(subjectRate, projectId);
        subjectRate.setRange(range);
        subjectRate.getSubjectRates().addAll(subjectRateList);

        return subjectRate;
    }


}
