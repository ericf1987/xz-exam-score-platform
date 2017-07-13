package com.xz.scorep.executor.expressReport.manager;

import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.expressReport.service.ExpressReportTaskService;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/12.
 */
@Component
public class ExpressReportManager {

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    ExpressReportTaskService expressReportTaskService;

    static final Logger LOG = LoggerFactory.getLogger(ExpressReportManager.class);

    public void startTask(final String projectId, final String subjectId){

        //清理项目缓存
        LOG.info("开始清理项目缓存：{}", projectId);
        cacheFactory.removeProjectCache(projectId);
        LOG.info("清理完成：{}", projectId);

        //学校ID列表
        List<String> schoolIds = schoolService.listSchool(projectId).stream().map(ProjectSchool::getId)
                .collect(Collectors.toList());
        //所有参考科目
        List<String> examSubjects = !StringUtils.isEmpty(subjectId) ? Collections.singletonList(subjectId) :
                subjectService.listSubjects(projectId).stream().map(s -> s.getId()).collect(Collectors.toList());

        LOG.info("==========开始分发快报生成任务==========");
        LOG.info("当前项目：{}, 科目：{}", projectId, examSubjects.toString());

        for (String schoolId : schoolIds) {
            List<String> classIds = classService.listClasses(projectId, schoolId).stream().map(c -> c.getId()).collect(Collectors.toList());
            for (String classId : classIds) {
                for(String sid : examSubjects){
                    expressReportTaskService.dispatchOneClassOneSubject(projectId, schoolId, classId, sid);
                }
            }
        }
        LOG.info("==========分发完成==========");
    }
}
