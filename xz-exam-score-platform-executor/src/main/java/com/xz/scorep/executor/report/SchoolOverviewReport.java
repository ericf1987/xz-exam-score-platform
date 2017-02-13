package com.xz.scorep.executor.report;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.util.StringUtil;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SchoolOverviewReport extends AbstractReport {

    private static final String AVG_PROVINCE_SUBJECT_TEMPLATE = "select " +
            "'{{subject}}' as subject, avg(score) as average from score_subject_{{subject}}";

    private static final String AVG_SCHOOL_SUBJECT_TEMPLATE = "select " +
            "'{{subject}}' as subject, avg(score) as average from score_subject_{{subject}} where student_id in(\n" +
            "  select id from student where class_id in (\n" +
            "    select id from class where school_id=?\n" +
            "  )\n" +
            ")";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        // 各科平均分
        Map<String, String> schoolSubjectAverages;
        if (StringUtil.isEmpty(schoolId) || schoolId.equals("0")) {
            schoolSubjectAverages = getProvinceSubjectAverages(projectId, projectDao);
        } else {
            schoolSubjectAverages = getSchoolSubjectAverages(projectId, projectDao, schoolId);
        }

        // 总分人数、最低分、最高分、平均分、优率、良率、合格率


        // 返回结果
        HashMap<Object, Object> result = new HashMap<>();
        result.put("schoolSubjectAverages", schoolSubjectAverages);
        return result;
    }

    // 全省各科平均分
    private Map<String, String> getProvinceSubjectAverages(String projectId, DAO projectDao) {
        List<String> subjectQueries = new ArrayList<>();

        subjectService.querySubjectIds(projectId).forEach(subject -> {
            String subjectId = subject.getId();
            subjectQueries.add(AVG_PROVINCE_SUBJECT_TEMPLATE.replace("{{subject}}", subjectId));
        });

        String finalQuery = String.join(" union ", subjectQueries);

        List<Row> rows = projectDao.query(finalQuery);

        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("subject"),
                row -> row.getString("average")
        ));
    }

    // 学校各科平均分
    private Map<String, String> getSchoolSubjectAverages(String projectId, DAO projectDao, String schoolId) {
        List<String> subjectQueries = new ArrayList<>();

        subjectService.querySubjectIds(projectId).forEach(subject -> {
            String subjectId = subject.getId();
            subjectQueries.add(AVG_SCHOOL_SUBJECT_TEMPLATE.replace("{{subject}}", subjectId));
        });

        String finalQuery = String.join(" union ", subjectQueries);
        String[] params = new String[subjectQueries.size()];
        Arrays.fill(params, schoolId);

        List<Row> rows = projectDao.query(finalQuery, (Object[]) params);

        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("subject"),
                row -> row.getString("average")
        ));
    }
}
