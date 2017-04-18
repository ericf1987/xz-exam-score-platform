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
        SUBJECT_NAMES.put("004005006", "理科综合");
        SUBJECT_NAMES.put("007008009", "文科综合");
        SUBJECT_NAMES.put("004005", "物化综合");
        SUBJECT_NAMES.put("007008", "政史综合");
        SUBJECT_NAMES.put("006009", "生地综合");
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
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0,real_score decimal(4,1) not null default 0)");

        projectDao.execute("create table if not exists " +
                "score_objective_" + subjectId +
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0)");

        projectDao.execute("create table if not exists " +
                "score_subjective_" + subjectId +
                "(student_id VARCHAR(36) primary key,score decimal(4,1) not null default 0)");
    }

    public List<ExamSubject> listSubjects(String projectId) {
        SimpleCache cache = cacheFactory.getProjectCache(projectId);
        String cacheKey = "subjects:";
        return cache.get(cacheKey, () ->
                new ArrayList<>(daoFactory.getProjectDao(projectId)
                        .query(ExamSubject.class, "select * from subject")));
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
        String sql = "select sum(full_score) as full_score from quest " +
                "where quest_subject = '{{subjectId}}' " +
                "and quest_no != {{exclude}} ";
        String temp = String.join(" and quest_no != ", excludeQuestNos);
        return sql.replace("{{subjectId}}", subjectId).replace("{{exclude}}", temp);

    }

}
