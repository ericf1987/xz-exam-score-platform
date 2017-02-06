package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.StudentService;

import java.util.ArrayList;
import java.util.List;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ImportStudentHelper {

    public static void importStudentList(
            AppAuthClient appAuthClient, Context context,
            SchoolService schoolService, ClassService classService, StudentService studentService) {

        importSchools(context, appAuthClient, schoolService);
        importClasses(context, appAuthClient, classService);
        importStudents(context, appAuthClient, studentService);
    }

    private static void importSchools(Context context, AppAuthClient appAuthClient, SchoolService schoolService) {

        List<ProjectSchool> contextSchools = new ArrayList<>();
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
            contextSchools.add(school);
            schoolService.saveSchool(projectId, school);
        }
    }

    private static void importClasses(Context context, AppAuthClient appAuthClient, ClassService classService) {

        List<ProjectClass> contextClasses = new ArrayList<>();
        context.put("classes", contextClasses);

        String projectId = context.get("projectId");
        List<ProjectSchool> contextSchools = context.get("schools");

        // 清空班级列表
        classService.clearClasses(projectId);

        // 重新保存班级列表
        for (ProjectSchool school : contextSchools) {
            Result result = appAuthClient.callApi("QueryExamClassByProject",
                    new Param().setParameter("projectId", projectId)
                            .setParameter("schoolId", school.getId())
                            .setParameter("needStudentCount", false));

            JSONArray classes = result.get("classes");
            JSONUtils.<JSONObject>forEach(classes, o -> {
                o.put("schoolId", school.getId());   // 补充作为 ProjectClass 构造方法的参数
                ProjectClass c = new ProjectClass(o);
                contextClasses.add(c);
                classService.saveClass(projectId, c);
            });
        }

    }

    private static void importStudents(Context context, AppAuthClient appAuthClient, StudentService studentService) {
        String projectId = context.getString("projectId");
        List<ProjectClass> contextClasses = context.get("classes");

        // 清空考生列表
        studentService.clearStudents(projectId);

        for (ProjectClass projectClass : contextClasses) {
            Result result = appAuthClient.callApi("QueryClassExamStudent",
                    new Param().setParameter("projectId", projectId)
                            .setParameter("classId", projectClass.getId()));

            JSONArray examStudents = result.get("examStudents");
            JSONUtils.<JSONObject>forEach(examStudents, s -> {
                ProjectStudent student = new ProjectStudent(s);
                studentService.saveStudent(projectId, student);
            });
        }
    }

}
