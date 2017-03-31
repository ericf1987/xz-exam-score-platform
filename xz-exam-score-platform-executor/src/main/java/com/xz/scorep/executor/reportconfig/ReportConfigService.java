package com.xz.scorep.executor.reportconfig;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportConfigService {

    private static final String DEFAULT_PROJECT_ID = "[DEFAULT]";

    private static final String DELETE_REPORT_CONFIG = "delete from report_config where project_id=?";

    @Autowired
    private DAOFactory daoFactory;

    public void saveReportConfig(final ReportConfig reportConfig) {
        DAO.runTransactionWithException(() -> {
            DAO managerDao = daoFactory.getManagerDao();
            managerDao.execute(DELETE_REPORT_CONFIG, reportConfig.getProjectId());
            managerDao.insert(reportConfig, "report_config");
        });
    }

    // 查询指定项目的报表配置，并与缺省报表配置合并
    public ReportConfig queryReportConfig(String projectId) {
        ReportConfig defaultReportConfig = queryRawReportConfig(DEFAULT_PROJECT_ID);
        ReportConfig projectReportConfig = queryRawReportConfig(projectId);

        combine(projectReportConfig, defaultReportConfig);
        return projectReportConfig;
    }

    // 查询指定项目的报表配置。如果数据库记录不存在，则返回一个只包含 projectId 属性的
    private ReportConfig queryRawReportConfig(String projectId) {

        ReportConfig reportConfig = daoFactory.getManagerDao().queryFirst(
                ReportConfig.class,
                "select * from report_config where project_id=?", projectId);

        if (reportConfig == null) {
            reportConfig = new ReportConfig();
            reportConfig.setProjectId(projectId);
        }

        return reportConfig;
    }

    // 合并两个 ReportConfig 对象
    private void combine(ReportConfig projectReportConfig, ReportConfig defaultReportConfig) {
        if (projectReportConfig.getCollegeEntryLevel() == null) {
            projectReportConfig.setCollegeEntryLevel(defaultReportConfig.getCollegeEntryLevel());
        }
        if (projectReportConfig.getCollegeEntryLevelEnabled() == null) {
            projectReportConfig.setCollegeEntryLevelEnabled(defaultReportConfig.getCollegeEntryLevelEnabled());
        }
        if (projectReportConfig.getCombineCategorySubjects() == null) {
            projectReportConfig.setCombineCategorySubjects(defaultReportConfig.getCombineCategorySubjects());
        }
        if (projectReportConfig.getEntryLevelStatType() == null) {
            projectReportConfig.setEntryLevelStatType(defaultReportConfig.getEntryLevelStatType());
        }
        if (projectReportConfig.getHighScoreRate() == 0) {
            projectReportConfig.setHighScoreRate(defaultReportConfig.getHighScoreRate());
        }
        if (projectReportConfig.getRankLevelCombines() == null) {
            projectReportConfig.setRankLevelCombines(defaultReportConfig.getRankLevelCombines());
        }
        if (projectReportConfig.getRankLevels() == null) {
            projectReportConfig.setRankLevels(defaultReportConfig.getRankLevels());
        }
        if (projectReportConfig.getRankSegmentCount() == 0) {
            projectReportConfig.setRankSegmentCount(defaultReportConfig.getRankSegmentCount());
        }
        if (projectReportConfig.getScoreLevels() == null) {
            projectReportConfig.setScoreLevels(defaultReportConfig.getScoreLevels());
        }
        if (projectReportConfig.getSeparateCategorySubjects() == null) {
            projectReportConfig.setSeparateCategorySubjects(defaultReportConfig.getSeparateCategorySubjects());
        }
        if (projectReportConfig.getShareSchoolReport() == null) {
            projectReportConfig.setShareSchoolReport(defaultReportConfig.getShareSchoolReport());
        }
        if (projectReportConfig.getTopStudentRate() == 0) {
            projectReportConfig.setTopStudentRate(defaultReportConfig.getTopStudentRate());
        }
    }
}
