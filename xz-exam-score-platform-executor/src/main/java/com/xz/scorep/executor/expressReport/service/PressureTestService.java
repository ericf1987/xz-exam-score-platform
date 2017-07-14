package com.xz.scorep.executor.expressReport.service;

import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.pss.bean.PssForStudent;
import com.xz.scorep.executor.pss.service.PssService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author by fengye on 2017/7/13.
 */
@Service
public class PressureTestService {

    @Autowired
    PssService pssService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    StudentQuery studentQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Autowired
    static final Logger LOG = LoggerFactory.getLogger(PressureTestService.class);

    public void startPressureTest1(String pdfName, String relativePath, String createUrl, String threadCount) {
        int count = Integer.valueOf(threadCount);

        List<PdfTask> tasks = new ArrayList<>();

        long begin = System.currentTimeMillis();

        IntStream.rangeClosed(0, count).forEach(i -> {
            PdfTask pdfTask = new PdfTask(pdfName + "_" + i + ".pdf", relativePath, createUrl, false);
            pdfTask.start();
            tasks.add(pdfTask);
            LOG.info("线程：{} 开始作业 文件名：{}，相对路径：{}，URL：{}", pdfTask.getName(), pdfTask.getPdfName(), pdfTask.getRelativePath(), pdfTask.getCreateUrl());
        });

        for (PdfTask task : tasks) {
            try {
                task.join();
            } catch (InterruptedException e) {
                LOG.error("线程：{} 执行异常！", task.getName());
            }
        }

        long end = System.currentTimeMillis();

        LOG.info("测试完成，耗时：{}", end - begin);


    }

    //一个线程处理多个学生
    public void startPressureTest2(String projectId, String schoolId, String classId, String subjectId){

        long begin = System.currentTimeMillis();

        List<String> classIds = !StringUtils.isEmpty(classId) ? Collections.singletonList(classId) :
                classService.listClasses(projectId, schoolId).stream().map(c -> c.getId()).collect(Collectors.toList());

        List<String> subjectIds = !StringUtils.isEmpty(subjectId) ? Collections.singletonList(subjectId) :
                subjectService.listSubjects(projectId).stream().map(s -> s.getId()).collect(Collectors.toList());

        List<Thread> tasks = new ArrayList<>();

        classIds.forEach(c -> subjectIds.forEach(s -> {
            List<String> studentList = studentQuery.getStudentList(projectId, Range.clazz(c));
            List<PssForStudent> PssForStudents = pssService.packPssForStudents(projectId, schoolId, c, s, studentList);

            Thread t = new Thread(() -> {
                pssService.processResultData(PssForStudents);
            });
            t.start();
            LOG.info("线程：{}， 开始生成 ：项目{}， 学校{}， 班级{}， 科目{}, 学生总数{}", t.getName() , projectId, schoolId, c, s, studentList.size());
            tasks.add(t);
        }));

        for (Thread task : tasks) {
            try {
                task.join();
                LOG.info("线程：{} 执行完毕！", task.getName());
            } catch (InterruptedException e) {
                LOG.error("线程：{} 执行异常！", task.getName());
            }
        }

        long end = System.currentTimeMillis();

        LOG.info("测试完成，耗时：{}", end - begin);

    }



    class PdfTask extends Thread {

        private String pdfName;

        private String relativePath;

        private String createUrl;

        private boolean isVertical;

        public String getPdfName() {
            return pdfName;
        }

        public void setPdfName(String pdfName) {
            this.pdfName = pdfName;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public String getCreateUrl() {
            return createUrl;
        }

        public void setCreateUrl(String createUrl) {
            this.createUrl = createUrl;
        }

        public boolean isVertical() {
            return isVertical;
        }

        public void setVertical(boolean vertical) {
            isVertical = vertical;
        }

        public PdfTask() {

        }

        public PdfTask(String fileName, String relativePath, String createUrl, boolean isVertical) {
            this.pdfName = fileName;
            this.relativePath = relativePath;
            this.createUrl = createUrl;
            this.isVertical = isVertical;
        }

        @Override
        public void run() {
            pssService.sendToPDFByPost(this.relativePath, this.pdfName, this.createUrl);
        }
    }
}
