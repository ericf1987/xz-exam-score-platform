package com.xz.scorep.executor.expressReport.service;

import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.pss.service.PssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author by fengye on 2017/7/12.
 */
@Service
public class ExpressReportTaskService {

    @Autowired
    ProjectService projectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Value("${pdf.expressreport.url}")
    private String server;

    @Autowired
    PssService pssService;

    static final Logger LOG = LoggerFactory.getLogger(ExpressReportTaskService.class);

    /**
     * 生成
     * @param projectId    项目ID
     * @param schoolId     学校ID
     * @param classId      班级ID
     * @param subjectId    科目ID
     */
    public void dispatchOneClassOneSubject(String projectId, String schoolId, String classId, String subjectId) {

        String savePath = StringUtil.joinPathsWith("/", "expressReport",
                projectService.findProject(projectId).getName() + "(" + projectId + ")",
                schoolService.findSchool(projectId, schoolId).getName(),
                SubjectService.getSubjectName(subjectId)
        );

        ProjectClass aClass = classService.findClass(projectId, classId);
        String fileName = aClass.getName() + "_" + aClass.getId() + ".pdf";

        String url = getPDFServerURL(projectId, schoolId, classId, subjectId);

        pssService.sendToPDFByPost(savePath, fileName, url);
    }

    private String getPDFServerURL(String projectId, String schoolId, String classId, String subjectId) {
        StringBuilder builder = new StringBuilder();
        builder.append("projectId=").append(projectId).append("&")
                .append("schoolId=").append(schoolId).append("&")
                .append("classId=").append(classId).append("&")
                .append("subjectId=").append(subjectId);
        try {
            String url = URLEncoder.encode(builder.toString(), "UTF-8");
            return server + url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
