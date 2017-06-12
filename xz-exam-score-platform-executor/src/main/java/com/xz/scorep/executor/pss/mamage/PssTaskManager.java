package com.xz.scorep.executor.pss.mamage;

import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.pss.service.PssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/5/24.
 */
@Component
public class PssTaskManager {
    static final Logger LOG = LoggerFactory.getLogger(PssTaskManager.class);

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    PssService pssService;

    @Autowired
    SubjectService subjectService;

    private ThreadPoolExecutor threadPoolExecutor;

    @PostConstruct
    public void init() {
        this.threadPoolExecutor = Executors.newBlockingThreadPoolExecutor(10, 10, 100);
    }

    public void startPssTask(final String projectId, Map<String, Object> configFromCMS, boolean async) {
        //学校ID列表
        List<String> schoolIds = schoolService.listSchool(projectId).stream().map(ProjectSchool::getId)
                .collect(Collectors.toList());
        //所有参考科目
        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<Map<String, Object>> list = new ArrayList<>();
        schoolIds.forEach(schoolId -> {
            Map<String, Object> map = new HashMap<>();
            map.put(schoolId, classService.listClasses(projectId, schoolId).stream().map(ProjectClass::getId).collect(Collectors.toList()));
            list.add(map);
        });

        LOG.info("====项目{}======, 生成学生成绩报告任务开始执行======", projectId);
        list.forEach(l -> {
            for (String schoolId : l.keySet()) {
                LOG.info("====项目{}, 学校{}, 生成开始", projectId, schoolId);
                List<String> classIds = (List<String>)l.get(schoolId);
                CountDownLatch countDownLatch = new CountDownLatch(classIds.size());

                for (String classId : classIds) {
                    Runnable runnable = () -> {
                        try {
                            pssService.dispatchOneClassTask(projectId, schoolId, classId, examSubjects, configFromCMS);
                        } catch (Exception e) {
                            LOG.info("班级生成失败, 项目{}， 学校{}， 班级{}", projectId, schoolId, classId);
                        } finally {
                            countDownLatch.countDown();
                        }
                    };

                    threadPoolExecutor.submit(runnable);
                }

                if (!async) {
                    try {
                        threadPoolExecutor.shutdown();
                        threadPoolExecutor.awaitTermination(1, TimeUnit.DAYS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    countDownLatch.await(1, TimeUnit.HOURS);
                    LOG.info("====项目{}, 学校{}, 试卷截图生成完毕", projectId, schoolId);
                } catch (InterruptedException e) {
                    LOG.info("====项目{}, 学校{}, 试卷截图生成超时！", projectId, schoolId);
                }

            }
        });

        LOG.info("====项目{}======, 试卷截图任务执行完毕！======", projectId);

    }

}
