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

        studentCounter.incre();
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

}
