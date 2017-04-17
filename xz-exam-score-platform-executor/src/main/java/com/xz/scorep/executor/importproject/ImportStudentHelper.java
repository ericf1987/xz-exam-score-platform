package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
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
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;

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
        String projectId = context.get("projectId");
        MongoClient client = context.get("client");

        clearBaseInfo(projectId);

        MongoDatabase projectDb = client.getDatabase(projectId);
        Map<String, ProjectSchool> schools = new HashMap<>();

        projectDb.getCollection("schools")
                .find(doc())
                .forEach((Consumer<Document>) doc -> importSchools(projectId, schools, doc));

        projectDb.getCollection("studentForProject")
                .find(doc())
                .forEach((Consumer<Document>) doc -> importStudentAndClass(projectId, schools, doc));

        this.studentCounter.finish();
    }

    private void importStudentAndClass(String projectId, Map<String, ProjectSchool> schools, Document document) {

        String classId = document.getString("classId");
        String className = document.getString("className");
        String schoolId = document.getString("schoolId");

        ProjectSchool school = schools.get(schoolId);
        String area = school.getArea();
        String city = school.getCity();
        String province = school.getProvince();

        ProjectClass projectClass = new ProjectClass(classId, className, schoolId, area, city, province);
        classService.saveClass(projectId, projectClass);

        String studentId = document.getString("studentId");
        String studentName = document.getString("studentName");
        String examNo = document.getString("examNo");
        String schoolStudentNo = document.getString("schoolStudentNo");
        ProjectStudent student = new ProjectStudent(studentId, examNo,
                schoolStudentNo, studentName, classId, schoolId, area, city, province);
        studentService.saveStudent(projectId, student);


    }

    private void importSchools(String projectId, Map<String, ProjectSchool> schools, Document document) {
        String schoolId = document.getString("schoolId");
        String schoolName = document.getString("schoolName");
        String area = document.getString("areaId");
        String city = document.getString("cityId");
        String province = document.getString("provinceId");

        ProjectSchool school = new ProjectSchool(schoolId, schoolName, area, city, province);
        schools.put(schoolId, school);
        schoolService.saveSchool(projectId, school);

    }

    private void clearBaseInfo(String projectId) {
        // 清空学校列表
        schoolService.clearSchools(projectId);
        // 清空班级列表
        classService.clearClasses(projectId);
        // 清空考生列表
        studentService.clearStudents(projectId);
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
