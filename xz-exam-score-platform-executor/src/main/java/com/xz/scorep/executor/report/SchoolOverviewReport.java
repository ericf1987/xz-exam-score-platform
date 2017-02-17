package com.xz.scorep.executor.report;

import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.aggritems.MinMaxQuery;
import com.xz.scorep.executor.aggritems.ScoreLevelRateQuery;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.table.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SchoolOverviewReport extends AbstractReport {

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private AverageQuery averageQuery;

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private MinMaxQuery minMaxQuery;

    @Autowired
    private ScoreLevelRateQuery scoreLevelRateQuery;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId) {

        // 各科平均分
        Map<String, String> schoolSubjectAverages = averageQuery.getSchoolSubjectAverages(projectId, schoolId);

        //////////////////////////////////////////////////////////////

        Table schoolTable = new Table("school_id");
        schoolTable.setValue(schoolId, "school_name", schoolService.findSchool(projectId, schoolId).getName());
        schoolTable.setValue(schoolId, "student_count", studentQuery.getSchoolStudentCount(projectId, schoolId));
        schoolTable.setValue(schoolId, "average", averageQuery.getSchoolProjectAverage(projectId, schoolId));
        schoolTable.readRow(scoreLevelRateQuery.getSchoolProjectSLR(projectId, schoolId));
        schoolTable.readRow(minMaxQuery.getSchoolProjectMinMax(projectId, schoolId));

        //////////////////////////////////////////////////////////////

        Table classTable = new Table("class_id");

        classService.listClasses(projectId, schoolId).forEach(
                c -> classTable.setValue(c.getId(), "class_name", c.getName()));

        studentQuery.getClassStudentCount(projectId, schoolId).entrySet().forEach(
                entry -> classTable.setValue(entry.getKey(), "student_count", entry.getValue()));

        minMaxQuery.getClassProjectMinMax(projectId, schoolId).forEach(
                classTable::readRow);

        averageQuery.getClassProjectAverages(projectId, schoolId).forEach(
                (classId, average) -> classTable.setValue(classId, "average", average));

        scoreLevelRateQuery.getClassProjectSLRs(projectId, schoolId).forEach(
                classTable::readRow);

        classTable.sortBy("class_name");

        //////////////////////////////////////////////////////////////

        // 返回结果
        HashMap<Object, Object> result = new HashMap<>();
        result.put("schoolSubjectAverages", schoolSubjectAverages);
        result.put("schoolProjectOverviewTable", schoolTable);
        result.put("classProjectOverviewTable", classTable);
        return result;
    }
}
