package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Counter;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ImportStudentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ImportStudentHelper.class);

    private AppAuthClient appAuthClient;

    private SchoolService schoolService;

    private ClassService classService;

    private StudentService studentService;

    private Counter studentCounter = new Counter(
            500, i -> LOG.info("已导入学生 {} 条", i));

    public ImportStudentHelper(
            AppAuthClient appAuthClient, SchoolService schoolService,
            ClassService classService, StudentService studentService
    ) {
        this.appAuthClient = appAuthClient;
        this.schoolService = schoolService;
        this.classService = classService;
        this.studentService = studentService;
    }

    public void importStudentList(Context context) {

        importSchools(context);
        importClasses(context);
        importStudents(context);

        this.studentCounter.finish();
    }

    private void importSchools(Context context) {

        Map<String, ProjectSchool> contextSchools = new HashMap<>();
        context.put("schools", contextSchools);

        String projectId = context.get("projectId");
        Result result = appAuthClient.callApi("QueryExamSchoolByProject",
                new Param().setParameter("projectId", projectId)
                        .setParameter("needStudentCount", false));

        // 清空学校列表
        schoolService.clearSchools(projectId);

        // 重新保存学校列表
        JSONArray schools = result.get("schools");
        for (int i = 0; i < schools.size(); i++) {
            ProjectSchool school = schools.getObject(i, ProjectSchool.class);
            contextSchools.put(school.getId(), school);
            schoolService.saveSchool(projectId, school);
        }
    }

    private void importClasses(Context context) {

        Map<String, ProjectClass> contextClasses = new HashMap<>();
        context.put("classes", contextClasses);

        String projectId = context.get("projectId");
        Map<String, ProjectSchool> contextSchools = context.get("schools");

        // 清空班级列表
        classService.clearClasses(projectId);

        // 重新保存班级列表
        List<ProjectClass> classList = new ArrayList<>();

        for (ProjectSchool school : contextSchools.values()) {
            Result result = appAuthClient.callApi("QueryExamClassByProject",
                    new Param().setParameter("projectId", projectId)
                            .setParameter("schoolId", school.getId())
                            .setParameter("needStudentCount", false));

            JSONArray classes = result.get("classes");
            JSONUtils.<JSONObject>forEach(classes, o -> {
                ProjectClass c = new ProjectClass(o);
                c.setSchoolId(school.getId());
                c.setArea(school.getArea());
                c.setCity(school.getCity());
                c.setProvince(school.getProvince());

                contextClasses.put(c.getId(), c);
                classList.add(c);
            });
        }

        classService.saveClass(projectId, classList);
    }

    private void importStudents(Context context) {
        String projectId = context.getString("projectId");
        Map<String, ProjectClass> contextClasses = context.get("classes");

        // 清空考生列表
        studentService.clearStudents(projectId);

        for (ProjectClass projectClass : contextClasses.values()) {
            Result result = appAuthClient.callApi("QueryClassExamStudent",
                    new Param().setParameter("projectId", projectId)
                            .setParameter("classId", projectClass.getId()));

            JSONArray examStudents = result.get("examStudents");
            List<ProjectStudent> studentList = new ArrayList<>();

            JSONUtils.<JSONObject>forEach(examStudents, s -> {
                ProjectStudent student = new ProjectStudent(s);
                student.setClassId(projectClass.getId());
                student.setSchoolId(projectClass.getSchoolId());
                student.setArea(projectClass.getArea());
                student.setCity(projectClass.getCity());
                student.setProvince(projectClass.getProvince());

                studentList.add(student);
                studentCounter.incre();
            });

            studentService.saveStudent(projectId, studentList);
        }
    }

}
