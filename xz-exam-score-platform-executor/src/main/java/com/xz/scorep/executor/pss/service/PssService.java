package com.xz.scorep.executor.pss.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.api.utils.HttpUtils;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.*;
import com.xz.scorep.executor.pss.bean.PssForStudent;
import com.xz.scorep.executor.pss.utils.PaintUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

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
    ScannerDBService scannerDBService;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ProjectService projectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    StudentService studentService;

    @Autowired
    SubjectService subjectService;

    @Value("${pdf.img.url}")
    private String imgUrl;

    @Value("${pdf.server.url}")
    private String serverUrl;

    public static final String BASE64_HEADER = "data:image/png;base64,";

    //封装多个任务
    public void dispatchOneClassTask(String projectBakId, String schoolId, String classId, List<String> examSubjects, Map<String, Object> configFromCMS) {
        List<PssTaskBean> pssTaskBeans = new ArrayList<>();
        for (String examSubject : examSubjects) {
            try {
                PssTaskBean pssTaskBean = packPssTask(projectBakId, schoolId, classId, examSubject, configFromCMS);
                pssTaskBean.start();
                pssTaskBeans.add(pssTaskBean);
            } catch (Exception e) {
                LOG.info("----分发任务失败：项目{}，学校{}，班级{}，科目{}", projectBakId, schoolId, classId, examSubject);
            }
        }

        joinAllPssTask(pssTaskBeans);
    }

    public void joinAllPssTask(List<PssTaskBean> pssTaskBeans) {
        for (PssTaskBean pssTaskBean : pssTaskBeans) {
            try {
                pssTaskBean.join();
            } catch (InterruptedException e) {
                LOG.error("线程中断，执行pss任务失败！");
            }
        }
    }

    //封装单个任务，每个任务执行一个学生列表
    private PssTaskBean packPssTask(String projectBakId, String schoolId, String classId, String subjectId, Map<String, Object> configFromCMS) {
        List<String> studentList = studentQuery.getStudentList(projectBakId, Range.clazz(classId));
        List<PssForStudent> pssForStudents = packPssForStudents(projectBakId, schoolId, classId, subjectId, studentList);
        return new PssTaskBean(pssForStudents);
    }

    public void runTaskByClassAndSubject(String projectId, String schoolId, String classId, String subjectId, Map<String, Object> configFromCMS) {
        String projectBakId = projectId + "_" + subjectId + "_bak";
        List<String> studentList = studentQuery.getStudentList(projectBakId, Range.clazz(classId));

        List<String> classIds = !StringUtils.isEmpty(classId) ? Collections.singletonList(classId) :
                classService.listClasses(projectId, schoolId).stream().map(c -> c.getId()).collect(Collectors.toList());

        List<String> subjectIds = !StringUtils.isEmpty(subjectId) ? Collections.singletonList(subjectId) :
                subjectService.listSubjects(projectId).stream().map(s -> s.getId()).collect(Collectors.toList());

        for (String cid : classIds) {
            for(String sid : subjectIds){
                List<String> studentList = studentQuery.getStudentList(projectId, Range.clazz(cid));

                List<PssForStudent> PssForStudents = packPssForStudents(projectId, schoolId, cid, sid, studentList);
        List<PssForStudent> PssForStudents = packPssForStudents(projectBakId, schoolId, classId, subjectId, studentList);

                processResultData(PssForStudents);

                LOG.info("--------数据生成成功：项目{}， 学校{}， 班级{}， 科目{}, 学生总数{}", projectId, schoolId, cid, sid, studentList.size());
            }
        }

    }

    //返回单个pss任务对应的学生列表
    private List<PssForStudent> packPssForStudents(String projectBakId, String schoolId, String classId, String subjectId, List<String> studentList) {
        List<PssForStudent> PssForStudents = new ArrayList<>();
        for (String studentId : studentList) {
            PssForStudent pssForStudent = new PssForStudent(
                    projectBakId, schoolId, classId, subjectId, studentId
            );
            PssForStudents.add(pssForStudent);
        }
        return PssForStudents;
    }

    //执行单个学生的pss报告打印
    public void runTaskByOneStudent(String projectId, String schoolId, String classId, String subjectId, String studentId, Map<String, Object> configFromCMS) {
        String projectBakId = projectId + "_" + subjectId + "_bak";
        PssForStudent pssForStudent = new PssForStudent(projectBakId, schoolId, classId, subjectId, studentId);
        processResultData(Collections.singletonList(pssForStudent));
    }

    /**
     * 发送任务请求至PDF服务器
     *
     * @param pssForStudents 任务群体
     */
    public void processResultData(List<PssForStudent> pssForStudents) {

        for (PssForStudent pssForStudent : pssForStudents) {

            String projectBakId = pssForStudent.getProjectId();
            int endIndex = projectBakId.indexOf("_") == -1 ? projectBakId.length() : projectBakId.indexOf("_");
            String projectId = projectBakId.substring(0, endIndex);
            String schoolId = pssForStudent.getSchoolId();
            String classId = pssForStudent.getClassId();
            String subjectId = pssForStudent.getSubjectId();
            String studentId = pssForStudent.getStudentId();

            schoolService.findSchool(projectBakId, schoolId).getName();

            /*String savePath = StringUtil.joinPathsWith("/", projectBakId, schoolId,
                    classId, subjectId);*/

            Row student = studentService.findStudent(projectBakId, studentId);

            String savePath = StringUtil.joinPathsWith("/",
                    projectService.findProject(projectId).getName() + "(" + projectBakId + ")",
                    schoolService.findSchool(projectBakId, schoolId).getName(),
                    classService.findClass(projectBakId, classId).getName(),
                    SubjectService.getSubjectName(subjectId)
            );

            String fileName = student.getString("name") + "_" + student.getString("exam_no") + ".pdf";

            String url = getURL(projectId, schoolId, classId, subjectId, studentId);
            sendToPDFByPost(savePath, fileName, url);
        }

    }

    /**
     * 发送请求到PDF服务器生成PDF文件
     *
     * @param savePath   文件保存相对路径
     * @param fileName   文件名
     * @param requestUrl PDF服务器请求URL
     */
    public void sendToPDFByPost(String savePath, String fileName, String requestUrl) {

        Map<String, String> params = new HashMap<>();
        params.put("relativePath", savePath);
        params.put("pdfName", fileName);
        params.put("isVertical", "false");
        params.put("createUrl", requestUrl);

        HttpUtils.sendRequest(serverUrl, params);
    }

    /**
     * 获取PDF转发服务器URL
     *
     * @param projectId 项目ID
     * @param schoolId  学校ID
     * @param classId   班级ID
     * @param subjectId 科目ID
     * @param studentId 学生ID
     * @return 返回URL
     */
    public String getURL(String projectId, String schoolId, String classId, String subjectId, String studentId) {
        StringBuilder builder = new StringBuilder();
        builder.append("projectId=").append(projectId).append("&")
                .append("schoolId=").append(schoolId).append("&")
                .append("classId=").append(classId).append("&")
                .append("subjectId=").append(subjectId).append("&")
                .append("studentId=").append(studentId).append("&");
        try {
            String url = URLEncoder.encode(builder.toString(), "UTF-8");
            return imgUrl + url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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
     * 返回学生正反面答题卡图片
     *
     * @param projectId      项目ID
     * @param subjectId      科目ID
     * @param studentId      学生ID
     * @param subjectRuleMap 参数显示规则
     * @return 返回结果
     */
    public Map<String, String> getStudentImgURL(String projectId, String subjectId,
                                                String studentId, Map<String, Object> subjectRuleMap) {
        Map<String, Object> studentCardSlices = scannerDBService.getOneStudentCardSlice(projectId, studentId, subjectId);
        Map<String, String> map = new HashMap<>();
        map.put("paper_positive", doConvert(MapUtils.getString(studentCardSlices, "paper_positive"), PaintUtils.PNG));
        map.put("paper_reverse", doConvert(MapUtils.getString(studentCardSlices, "paper_reverse"), PaintUtils.PNG));
        return map;
    }

    /**
     * 重新生成失败学生列表的报告
     *
     * @param projectId 项目ID
     */
    public void regenerateFail(String projectId) {
        LOG.info("========开始重新生成========");
        List<PssForStudent> pssForStudents = getFailStudent(projectId);
        LOG.info("任务数为：{}", pssForStudents.size());
        clearFailRecord(projectId);
        processResultData(pssForStudents);
        LOG.info("========分发完成========");
    }

    /**
     * 清理生成失败的数据记录
     *
     * @param projectId 项目ID
     */
    public void clearFailRecord(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("delete from pss_task_fail where project_id = ?", projectId);
    }

    /**
     * 获取生成失败的学生记录
     *
     * @param projectId 项目ID
     * @return 返回结果
     */
    public List<PssForStudent> getFailStudent(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.query(PssForStudent.class, "select * from pss_task_fail where project_id = ?", projectId);
    }

    class PssTaskBean extends Thread {
        List<PssForStudent> students;

        public PssTaskBean(List<PssForStudent> students) {
            this.students = students;
        }

        @Override
        public void run() {
            processResultData(students);
        }
    }

}
