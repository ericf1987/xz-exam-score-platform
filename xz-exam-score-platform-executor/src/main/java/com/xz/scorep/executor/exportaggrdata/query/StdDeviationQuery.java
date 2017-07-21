package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.StdDeviation;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.xz.scorep.executor.exportaggrdata.utils.AggrBeanUtils.setTarget;
import static com.xz.scorep.executor.utils.SqlUtils.chooseProjectOrSubject;

/**
 * @author by fengye on 2017/7/21.
 */
@Component
public class StdDeviationQuery {
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

    public static final String QUERY_DATA = "select * from std_deviation";

    static final Logger LOG = LoggerFactory.getLogger(StdDeviationQuery.class);

    public List<StdDeviation> queryObj(String projectId){

        LOG.info("开始查询 std_deviation 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<Row> rows = projectDao.query(QUERY_DATA);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<StdDeviation> result = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();
            doProcess(projectId, rows, projectSchools, projectClasses, result, subjectId);
        }

        LOG.info("查询完成 std_deviation 共 {} 条.....", result.size());

        return result;
    }

    private void doProcess(String projectId, List<Row> rows, List<ProjectSchool> projectSchools, List<ProjectClass> projectClasses, List<StdDeviation> result, String subjectId) {
        //总体数据
        String province = Range.PROVINCE_RANGE.getId();

        Optional<Row> row = rows.stream().filter(
                r -> province.equals(r.getString("range_id")) && chooseProjectOrSubject(projectId, subjectId, r)
        ).findFirst();

        if(row.isPresent()){
            result.add(packStdDeviation(projectId, subjectId, Range.PROVINCE, province, row.get()));
        }

        //学校数据
        for (ProjectSchool projectSchool : projectSchools) {
            String schoolId = projectSchool.getId();

            Optional<Row> data = rows.stream().filter(
                    r -> schoolId.equals(r.getString("range_id")) && chooseProjectOrSubject(projectId, subjectId, r)
            ).findFirst();

            if(data.isPresent()){
                result.add(packStdDeviation(projectId, subjectId, Range.SCHOOL, schoolId, data.get()));
            }
        }

        //班级数据
        for (ProjectClass projectClass : projectClasses) {
            String classId = projectClass.getId();

            Optional<Row> data = rows.stream().filter(
                    r -> classId.equals(r.getString("range_id")) && chooseProjectOrSubject(projectId, subjectId, r)
            ).findFirst();

            if(data.isPresent()){
                result.add(packStdDeviation(projectId, subjectId, Range.CLASS, classId, data.get()));
            }
        }
    }

    private StdDeviation packStdDeviation(String projectId, String subjectId, String rangeName, String rangeId, Row data) {

        StdDeviation stdDeviation = new StdDeviation();

        Range range = new Range();
        range.setId(rangeId);
        range.setName(rangeName);

        Target target = new Target();
        setTarget(projectId, subjectId, target);

        stdDeviation.setRange(range);
        stdDeviation.setTarget(target);
        stdDeviation.setAggrObject(stdDeviation, projectId);
        stdDeviation.setStdDeviation(data.getDouble("std_deviation", 0));

        return stdDeviation;
    }
}
