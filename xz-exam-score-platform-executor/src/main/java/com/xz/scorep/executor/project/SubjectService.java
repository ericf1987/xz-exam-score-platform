package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubjectService {

    private static final Map<String, String> SUBJECT_NAMES = new HashMap<>();

    static {
        SUBJECT_NAMES.put("", "全科");
        SUBJECT_NAMES.put("001", "语文");
        SUBJECT_NAMES.put("002", "数学");
        SUBJECT_NAMES.put("003", "英语");
        SUBJECT_NAMES.put("004", "物理");
        SUBJECT_NAMES.put("005", "化学");
        SUBJECT_NAMES.put("006", "生物");
        SUBJECT_NAMES.put("007", "政治");
        SUBJECT_NAMES.put("008", "历史");
        SUBJECT_NAMES.put("009", "地理");
        SUBJECT_NAMES.put("010", "社会");
        SUBJECT_NAMES.put("011", "科学");
        SUBJECT_NAMES.put("012", "技术与设计");
        SUBJECT_NAMES.put("013", "思想品德");
        SUBJECT_NAMES.put("014", "信息技术");
        SUBJECT_NAMES.put("015", "体育");
        SUBJECT_NAMES.put("016", "品生与品社");
        SUBJECT_NAMES.put("017", "小学综合");
        SUBJECT_NAMES.put("018", "学法知法");
        SUBJECT_NAMES.put("019", "道德与法治");
        SUBJECT_NAMES.put("020", "专业科目");
        SUBJECT_NAMES.put("021", "专业科目2");
        ////////////////////////////////////////////////////////////

        SUBJECT_NAMES.put("101", "种植专业");
        SUBJECT_NAMES.put("102", "英语专业");
        SUBJECT_NAMES.put("103", "医卫专业");
        SUBJECT_NAMES.put("104", "文秘专业");
        SUBJECT_NAMES.put("105", "师范专业");
        SUBJECT_NAMES.put("106", "商贸专业");
        SUBJECT_NAMES.put("107", "美术专业");
        SUBJECT_NAMES.put("108", "旅游专业");
        SUBJECT_NAMES.put("109", "计算机专业");
        SUBJECT_NAMES.put("110", "专机电专业");
        SUBJECT_NAMES.put("111", "电子专业");
        SUBJECT_NAMES.put("112", "财会专业");

        ////////////////////////////////////////////////////////////
        SUBJECT_NAMES.put("004005006", "理科综合");
        SUBJECT_NAMES.put("007008009", "文科综合");
        SUBJECT_NAMES.put("001002", "语文数学综合");
        SUBJECT_NAMES.put("003004005", "英语物理化学综合");
        SUBJECT_NAMES.put("004005", "物理化学综合");
        SUBJECT_NAMES.put("007008", "政治历史综合");
        SUBJECT_NAMES.put("006009", "生物地理综合");
        SUBJECT_NAMES.put("020021", "专业科目12综合");
    }

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private CacheFactory cacheFactory;

    public void clearSubjects(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table subject");
    }

    public void saveSubject(String projectId, String subjectId, double fullScore) {
        ExamSubject subject = new ExamSubject(subjectId, getSubjectName(subjectId), fullScore);
        saveSubject(projectId, subject);
    }

    public void saveSubject(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.delete(subject, "subject");
        projectDao.insert(subject, "subject");
    }

    public void createSubjectScoreTables(String projectId) {
        listSubjects(projectId).forEach(subject -> createSubjectScoreTable(projectId, subject.getId()));
    }

    public void createSubjectScoreTable(String projectId, String subjectId) {

        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("create table if not exists " +
                "score_subject_" + subjectId +
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0,real_score decimal(4,1) not null default 0,paper_score_type VARCHAR(6))");

        projectDao.execute("create table if not exists " +
                "score_objective_" + subjectId +
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0,paper_score_type VARCHAR(6))");

        projectDao.execute("create table if not exists " +
                "score_subjective_" + subjectId +
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0,paper_score_type VARCHAR(6))");
    }

    //在科目拆分的情况,使用缓存可能导致查出的科目信息与预期的不相符
    public List<ExamSubject> listSubjects(String projectId) {
        return new ArrayList<>(daoFactory.getProjectDao(projectId)
                .query(ExamSubject.class, "select * from subject ORDER BY LENGTH(id)"));
    }

    public static String getSubjectName(String subjectId) {
        return SUBJECT_NAMES.get(subjectId);
    }

    public ExamSubject findSubject(String projectId, String subjectId) {
        SimpleCache cache = cacheFactory.getProjectCache(projectId);
        String cacheKey = "subject:" + subjectId;

        return cache.get(cacheKey, () ->
                daoFactory.getProjectDao(projectId).queryFirst(
                        ExamSubject.class, "select * from subject where id=?", subjectId));
    }

    public double getSubjectScore(String projectId, String subjectId) {
        if (subjectId.equals("000")) {
            return daoFactory.getProjectDao(projectId)
                    .queryFirst("select sum(full_score) as full_score from subject")
                    .getDouble("full_score", 0);

        } else {
            return findSubject(projectId, subjectId).getFullScore();
        }
    }

    public double getSubSubjectScore(String projectId, String subjectId, String... excludeQuestNos) {
        return daoFactory.getProjectDao(projectId)
                .queryFirst(createSql(subjectId, excludeQuestNos))
                .getDouble("full_score", 0);
    }

    private String createSql(String subjectId, String[] excludeQuestNos) {

        if (excludeQuestNos == null || excludeQuestNos.length == 0) {
            String sql = "select sum(full_score) as full_score from quest " +
                    "where quest_subject = '{{subjectId}}' ";
            return sql.replace("{{subjectId}}", subjectId);
        }

        String sql = "select sum(full_score) as full_score from quest " +
                "where quest_subject = '{{subjectId}}' " +
                "and quest_no !=  {{exclude}} ";
        String temp = String.join(" and quest_no != ", excludeQuestNos);
        return sql.replace("{{subjectId}}", subjectId).replace("{{exclude}}", temp);
    }

    public void deleteSubject(String projectId, String subjectId) {
        daoFactory.getProjectDao(projectId).execute("delete from subject where id = ?", subjectId);
    }

    public boolean isVirtualSubject(String projectId, String subjectId) {
        ExamSubject subject = daoFactory.getProjectDao(projectId).queryFirst(
                ExamSubject.class, "select * from subject where id=?", subjectId);
        return Boolean.valueOf(subject.getVirtualSubject());
    }

    public ExamSubject findComplexSubject(String projectId, String subjectId) {
        String sql = "select * from subject where id like \"%{{subject}}%\" ";
        List<ExamSubject> query = daoFactory.getProjectDao(projectId).query(
                ExamSubject.class, sql.replace("{{subject}}", subjectId));
        return query.stream().filter(subject -> subject.getId().length() > 3).findFirst().get();
    }

}
