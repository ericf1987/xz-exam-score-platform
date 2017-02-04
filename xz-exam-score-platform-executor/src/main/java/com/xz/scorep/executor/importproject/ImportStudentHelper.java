package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.beans.user.School;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.project.SchoolService;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ImportStudentHelper {

    public static void importStudentList(
            AppAuthClient appAuthClient, Context context, SchoolService schoolService) {

        String projectId = context.get("projectId");
        Result result = appAuthClient.callApi("QueryExamSchoolByProject",
                new Param().setParameter("projectId", projectId)
                        .setParameter("needStudentCount", false));

        JSONArray schools = result.get("schools");
        importSchools(projectId, schools, schoolService);
    }

    private static void importSchools(String projectId, JSONArray schools, SchoolService schoolService) {
        for (int i = 0; i < schools.size(); i++) {
            School school = schools.getObject(i, School.class);
            schoolService.saveSchool(projectId, school);
        }
    }

}
