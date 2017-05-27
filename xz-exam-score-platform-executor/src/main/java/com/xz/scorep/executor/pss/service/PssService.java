package com.xz.scorep.executor.pss.service;

import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.project.ScannerDBService;
import com.xz.scorep.executor.project.StudentService;
import com.xz.scorep.executor.pss.utils.PaintUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * @author by fengye on 2017/5/24.
 */
@Service
public class PssService {

    static final Logger LOG = LoggerFactory.getLogger(PssService.class);

    @Autowired
    PaintService paintService;

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    StudentService studentService;

    @Autowired
    ScannerDBService scannerDBService;

    public static final String BASE64_HEADER = "data:image/png;base64,";

    public static final String URL = "http://192.168.1.58:8080/pss/showImg?";

    public static final boolean A4_MODE = true;

    public void dispatchOneClassTask(String projectId, String schoolId, String classId, List<ExamSubject> examSubjects, Map<String, Object> configFromCMS) {
        for (ExamSubject subject : examSubjects) {
            try {
                runTaskByClassAndSubject(projectId, schoolId, classId, subject, configFromCMS);
            } catch (Exception e) {
                LOG.info("----生成失败：项目{}，学校{}，班级{}，科目{}", projectId, schoolId, classId, subject.getId());
            }
        }
    }

    private void runTaskByClassAndSubject(String projectId, String schoolId, String classId, ExamSubject examSubject, Map<String, Object> configFromCMS) {
        List<String> studentList = studentQuery.getStudentList(projectId, Range.clazz(classId));

        Map<String, Map<String, String>> paramMap = new HashMap<>();
        for (String studentId : studentList) {
            Map<String, Object> oneStudentCardSlice = scannerDBService.getOneStudentCardSlice(projectId, studentId, examSubject.getId());
            String paper_positive = MapUtils.getString(oneStudentCardSlice, "paper_positive", "");
            String paper_reverse = MapUtils.getString(oneStudentCardSlice, "paper_reverse", "");

            //将图片转码成字符串
            String positive_img_string = doConvert(paper_positive, PaintUtils.PNG);
            String reverse_img_string = doConvert(paper_reverse, PaintUtils.PNG);

            Map<String, String> studentMap = new HashMap<>();
            studentMap.put("positive", positive_img_string);
            studentMap.put("reverse", reverse_img_string);
            paramMap.put(studentId, studentMap);
        }

        processResultData(projectId, schoolId, classId, examSubject.getId(), paramMap);

        LOG.info("--------数据生成成功：项目{}， 学校{}， 班级{}， 科目{}, 学生总数{}", projectId, schoolId, classId, examSubject.getId(), studentList.size());
    }

    private void processResultData(String projectId, String schoolId, String classId, String subjectId, Map<String, Map<String, String>> paramMap) {
        //生成图片保存的相对路径
        String savePath = StringUtil.joinPaths(projectId, schoolId, classId, subjectId);
        for (String studentId : paramMap.keySet()) {
            Map<String, String> map = paramMap.get(studentId);

            for (String key : map.keySet()) {
                String fileName = studentId + "_" + key + PaintUtils.SCREEN_SHOT_SUFFIX_PNG;
                String isPositive = "positive".equals(key) ? "true" : "false";
                String requestUrl = getURL(projectId, subjectId, studentId, isPositive, map.get(key));
                sendToPDF(savePath, fileName, requestUrl);
            }
        }
    }


    private void sendToPDF(String savePath, String fileName, String requestUrl) {

    }

    private String getURL(String projectId, String subjectId, String studentId, String isPositive, String imgUrl) {
        StringBuilder builder = new StringBuilder(URL);
        builder.append("projectId=").append(projectId).append("&")
                .append("subjectId=").append(subjectId).append("&")
                .append("studentId=").append(studentId).append("&")
                .append("isPositive=").append(isPositive);
        return builder.toString();
    }

    private String doConvert(String imgUrl, String format) {
        if (StringUtil.isBlank(imgUrl)) {
            LOG.error("图片URL不能为空！");
            return "";
        }
        try {
            BufferedImage bufferedImage = PaintUtils.loadImageUrl(imgUrl);
            return convertImgToString(bufferedImage, format);
        } catch (Exception e) {
            LOG.error("图片转化为字符串出现异常，图片格式为：{}", format);
            return "";
        }
    }

    /**
     * 将图片转化为字符串
     *
     * @param bufferedImage 图片对象
     * @param formatName    图片格式类型
     * @return 返回结果
     */
    public String convertImgToString(BufferedImage bufferedImage, String formatName) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, formatName, stream);
            byte[] bytes = stream.toByteArray();
            Base64.Encoder encoder = Base64.getEncoder();
            return BASE64_HEADER + encoder.encodeToString(bytes);
        } catch (IOException e) {
            LOG.error("图片转化为字符串出现异常，图片格式为：{}", formatName);
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 返回一个学生的答题卡单页截图数据
     *
     * @param projectId      项目ID
     * @param subjectId      科目ID
     * @param studentId      学生ID
     * @param isPositive     正反面
     * @param subjectRuleMap 参数显示规则
     * @return
     */
    public String getOneStudentOnePage(String projectId, String subjectId,
                                       String studentId, boolean isPositive,
                                       Map<String, Object> subjectRuleMap) {
        Map<String, Object> studentCardSlices = scannerDBService.getOneStudentCardSlice(projectId, studentId, subjectId);
        String imgUrl = isPositive ? MapUtils.getString(studentCardSlices, "paper_positive")
                : MapUtils.getString(studentCardSlices, "paper_reverse");
        return doConvert(imgUrl, PaintUtils.PNG);
    }

}
