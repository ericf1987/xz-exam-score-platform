package com.xz.scorep.executor.reportconfig;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportConfigService {

    public static final String DEFAULT_PROJECT_ID = "[DEFAULT]";

    @Autowired
    private DAOFactory daoFactory;

    public static final String DELETE_REPORT_CONFIG = "delete from report_config where project_id=?";

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
    public ReportConfig queryRawReportConfig(String projectId) {

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
        try {
            BeanUtils.fillProperties(projectReportConfig, defaultReportConfig);
        } catch (Exception e) {
            throw new ReportConfigException(e);
        }
    }
}
