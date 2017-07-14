package com.xz.scorep.executor.api.server.paperScreenShot;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.aggritems.StudentExamQuery;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.service.SubjectiveObjectiveQuery;
import com.xz.scorep.executor.api.service.SubjectiveObjectiveService;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.pss.service.PssService;
import com.xz.scorep.executor.utils.DataBaseUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 查询试卷截图留痕数据接口
 *
 * @author by fengye on 2017/5/22.
 */
@Function(description = "查询试卷截图留痕数据接口", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = true),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "studentId", type = Type.String, description = "学生ID", required = true)
})
@Service
public class PaperImgServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(PaperImgServer.class);

    @Autowired
    private PssService pssService;

    @Autowired
    private StudentExamQuery studentExamQuery;

    @Autowired
    private SubjectiveObjectiveQuery query;

    @Autowired
    private ProjectService projectService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    private SubjectiveObjectiveService subjectiveObjectiveService;

    @Autowired
    DAOFactory daoFactory;

    @Override
    public Result execute(Param param) {
        String projectId = param.getString("projectId");
        String schoolId = param.getString("schoolId");
        String classId = param.getString("classId");
        String subjectId = param.getString("subjectId");
        String studentId = param.getString("studentId");

        //获取可以正常连接的数据库...
        String database = DataBaseUtils.getDataBaseName(projectId, subjectId, daoFactory);
        LOG.info("本次查询得到的数据库为 {} ... ", database);
        //判断某个科目是否是拆分后的科目(并且从备份库中取相关数据....)
        boolean excludeSubject = query.isVirtualSubject(database, subjectId);
        if (excludeSubject) {
            LOG.info("项目ID {} ,科目ID {} 为拆分后科目,不进行答题留痕打印...", projectId, subjectId);
            return Result.success().set("hasData", false);
        }

        //判断某学生是否被排除(由于报表配置排除缺考,0分等可能导致学生没数据,故被排除)
        boolean exclude = query.studentIsExclude(database, subjectId, studentId);
        if (exclude) {
            LOG.info("项目ID {},科目ID {},学生ID{} 被排除...", projectId, subjectId, studentId);
            return Result.success().set("hasData", false);
        }
        //图片数据
        Map<String, String> studentImgURL = pssService.getStudentImgURL(database, subjectId, studentId, null);

        //图片获取失败的数据，做好记录
        checkAndRecord(database, schoolId, classId, subjectId, studentId, studentImgURL);

        String projectName = projectService.findProject(projectId).getName();
        Row studentRow = studentExamQuery.queryStudentInfo(database, studentId, subjectId);
        studentRow.put("project_name", projectName);

        //查询当前科目,从备份库中查询数据!
        ExamSubject subject = subjectService.findSubject(database, subjectId, false);

        //  学生主客观题得分详情(每一道题目得分,平均分,最高分或者班级得分率....)
        List<Map<String, Object>> objectiveList =
                subjectiveObjectiveService.queryObjectiveScoreDetail(database, subjectId, classId, studentId);
        List<Map<String, Object>> subjectiveList =
                subjectiveObjectiveService.querySubjectiveScoreDetail(database, subjectId, classId, studentId);

        //  学生得分(总分,科目得分,科目主客观题得分 .....)
        Map<String, Object> studentScore = studentExamQuery.queryStudentScore(database, subjectId, studentId);
        double subjectScore = 0;
        try {
            subjectScore = Double.valueOf(studentScore.get("subject_score").toString());
        } catch (Exception e) {
            LOG.error("项目ID {},科目ID {},学生ID {}获取科目分数异常....", projectId, subjectId, studentId);
        }

        //  学生超过班级平均分和超过学校平均分 .......
        Map<String, Object> overAverage = studentExamQuery.queryStudentOverAverage(database, subjectId, schoolId, classId, studentId);
        //  学生的班级排名和学校排名 .......
        Map<String, Object> rankMap = studentExamQuery.queryStudentRank(database, subjectId, studentId);

        //  学生的主客观题情况(主客观题满分,主客观题得分,学学生主客观题排名,主客观题班级平均分,主客观题班级最高分)
        Map<String, Object> objectiveScoreRank = subjectiveObjectiveService.objectiveDetail(database, subjectId, classId, studentId);
        Map<String, Object> subjectiveScoreRank = subjectiveObjectiveService.subjectiveDetail(database, subjectId, classId, studentId);

        //  学生主客观题与班级答对人数或班级平均分差距较大的TOP5
        List<Map<String, Object>> objectiveTop5 = subjectiveObjectiveService.queryObjectiveTop5(database, subjectId, classId, studentId);
        List<Map<String, Object>> subjectiveTop5 = subjectiveObjectiveService.querySubjectiveTop5(database, subjectId, studentId, classId);

        return Result.success()
                .set("hasData", true)
                .set("isFullScore", subject.getFullScore() == subjectScore)
                .set("imgString_positive", studentImgURL.get("paper_positive"))
                .set("imgString_reverse", studentImgURL.get("paper_reverse"))
                .set("subjectName", subject.getName())
                .set("studentInfo", studentRow)
                .set("objectiveList", objectiveList)
                .set("subjectiveList", subjectiveList)
                .set("studentScore", studentScore)
                .set("rankMap", rankMap)
                .set("overAverage", overAverage)
                .set("objectiveScoreRank", objectiveScoreRank)
                .set("subjectiveScoreRank", subjectiveScoreRank)
                .set("objectiveTop5", objectiveTop5)
                .set("subjectiveTop5", subjectiveTop5);
    }


    public void checkAndRecord(String database, String schoolId, String classId, String subjectId, String studentId, Map<String, String> studentImgURL) {
        String paper_positive = studentImgURL.get("paper_positive");
        String paper_reverse = studentImgURL.get("paper_reverse");
        if (StringUtils.isBlank(paper_positive) || StringUtils.isBlank(paper_reverse)) {
            DAO projectDao = daoFactory.getProjectDao(database);
/*            PssForStudent student = new PssForStudent(projectId, schoolId, classId, subjectId, studentId);
            projectDao.insert(student, "pss_task_fail");*/
            projectDao.runTransaction(() -> {
                projectDao.execute("delete from pss_task_fail where project_id = ? and school_id = ? and class_id = ? and subject_id = ? and student_id = ? ",
                        database, schoolId, classId, subjectId, studentId);
                projectDao.execute("insert into pss_task_fail (project_id, school_id, class_id, subject_id, student_Id) values (?, ?, ?, ?, ?) ",
                        database, schoolId, classId, subjectId, studentId);
            });
        }
    }


}
