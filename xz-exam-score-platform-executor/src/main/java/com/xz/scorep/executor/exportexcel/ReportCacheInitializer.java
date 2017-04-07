package com.xz.scorep.executor.exportexcel;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportCacheInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ReportCacheInitializer.class);

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

    public void initReportCache(String projectId) {
        initProvinceRank(projectId);
        LOG.info("获取province Cache 成功....  ");
        initSchoolRank(projectId);
        LOG.info("获取school Cache 成功....  ");
        initClassRank(projectId);
        LOG.info("获取class Cache 成功....  ");

    }

    private List<Row> initProvinceRank(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_province";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_province")));
    }


    private List<Row> initSchoolRank(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_school";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_school")));
    }


    private List<Row> initClassRank(String projectId) {
        SimpleCache cache = cacheFactory.getReportCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "rank_class";
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from rank_class")));
    }


    public List<Row> queryProvinceRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initProvinceRank(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "province_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Row> querySchoolRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initSchoolRank(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "school_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }

    public List<Row> queryClassRank(String projectId, List<String> student, String subjectId) {
        List<Row> result = initClassRank(projectId);
        return result.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")))
                .filter(row -> student.contains(row.getString("student_id")))
                .map(row -> {
                    String key = "class_rank_" + subjectId;
                    row.put(key, row.getString("rank"));
                    return row;
                })
                .collect(Collectors.toList());
    }

}
