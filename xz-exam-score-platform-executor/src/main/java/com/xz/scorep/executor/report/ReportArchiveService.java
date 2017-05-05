package com.xz.scorep.executor.report;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.aliyun.OSSFileClient;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.io.ZipFileCreator;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateService;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregationService;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.config.ExcelConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelReportManager;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ReportArchiveService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportArchiveService.class);


    private static final String AGGR_SQL = "select * from aggregation " +
            " where project_id = ? " +
            " ORDER BY start_time desc limit 1";

    //上次压缩包生成时间一定是在导出报表(生成Excel)之后的
    private static final String GENERATE_SQL = "select last_generate from report_archive " +
            " where project_id = ?" +
            " ORDER BY last_generate desc limit 1";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ExcelConfig excelConfig;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private AggregateService aggregateService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OSSFileClient ossFileClient;

    @Autowired
    private ExcelReportManager excelReportManager;

    // 全科打包和单科打包的通用逻辑
    public void startRunnable(final String projectId, Runnable runnable) {

        if (!projectService.updateProjectStatus(projectId, ProjectStatus.Ready, ProjectStatus.Archiving)) {
            throw new IllegalStateException("项目 " + projectId + " 正忙，无法执行打包");
        }

        Runnable _runnable = () -> {
            try {
                runnable.run();
            } finally {
                projectService.updateProjectStatus(projectId, ProjectStatus.Archiving, ProjectStatus.Ready);
            }
        };

        Thread thread = new Thread(_runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public void startProjectArchive(final String projectId) {
        startRunnable(projectId, () -> startProjectArchive0(projectId));
    }

    private void startProjectArchive0(String projectId) {
        LOG.info("开始给项目 " + projectId + " 打全科报表...");

        try {
            //是否有过basic统计
            if (!hasBasicAggregate(projectId)) {
                //删除之前的url记录
                deleteLastUrl(projectId, null);
                runBasicAggregate(projectId);
            }

            //上次basic统计之后是否有过生成Excel记录
            boolean generate = hasGenerateAfterBasicAggr(projectId);
            if (generate) {
                LOG.info("项目{}上次统计之后并无生成Excel记录,并生成本次Excel报表............",projectId);
                excelReportManager.generateReports(projectId, false, true);
            }

            if (generate){
                LOG.info("生成新的Excel报表,需要重新打包上传....");
                LOG.info("项目 " + projectId + " 开始打包报表...");
                String excelPath = excelConfig.getSavePath();
                String archiveRoot = ExcelReportManager.getSaveFilePath(projectId, excelPath, "全科报表");
                ExamProject project = projectService.findProject(projectId);
                File tempFile = createZipArchive(archiveRoot);
                String fileName = project.getName() + "_全科.zip";
                String uploadPath = uploadZipArchive(projectId, tempFile, fileName);
                LOG.info("报表打包完毕，已上传到 " + uploadPath);
                saveProjectArchiveRecord("000", projectId, uploadPath);
            }


        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            LOG.info("项目 " + projectId + " 全科报表打包结束。");
        }
    }

    private boolean hasBasicAggregate(String projectId) {
        Row lastBasicAggr = aggregationService.getAggregateStatus(projectId, AggregateType.Basic);
        if (lastBasicAggr == null) {
            LOG.info("项目ID:{}  尚未有过Basic统计记录......", projectId);
            return false;
        }

        Row lastAggr = daoFactory.getManagerDao().queryFirst(AGGR_SQL, projectId);
        if (lastAggr.getString("aggr_type").equals(AggregateType.Basic.name())) {
            LOG.info("项目ID:{}  上次统计记录为Basic统计.....", projectId);
            return true;
        }

        long lastAggrTime = lastAggr.getLong("start_time", 0);//上次统计时间
        long lastBasicTime = lastBasicAggr.getLong("start_time", 0);//上次basic统计时间
        if (lastBasicTime < lastAggrTime) {//上次basic统计之后  还有统计记录，则需要重新basic统计一次
            LOG.info("项目ID:{}  上次Basic统计记录之后还有过统计记录......", projectId);
            return false;
        }
        return true;
    }

    /**
     * 该判断并无逻辑问题,问题在于测试页面调用  "/export/excel"接口只生成Excel,
     * 并无后续压缩打包操作,而数据库 则在压缩之后记录在 report_archive 表中
     *
     * @param projectId 项目ID
     * @return 上次统计之后是否生成过Excel报表  (生成压缩报表)
     */
    private boolean hasGenerateAfterBasicAggr(String projectId) {

        Row aggrRow = daoFactory.getManagerDao().queryFirst(AGGR_SQL, projectId);

        Row generateRow = daoFactory.getManagerDao().queryFirst(GENERATE_SQL, projectId);

        //没有统计过或者没有生成过报表,则必须生成
        if (aggrRow == null || generateRow == null) {
            return true;
        }

        long aggrTime = aggrRow.getLong("start_time", 0);

        long generateTime = generateRow.getLong("last_generate", 0);

        LOG.info("上次统计时间:{} , 上次生成报表时间:{} ", aggrTime, generateTime);
        //上次生成Excel之后再无统计记录
        if (generateTime >= aggrTime) {
            return false;
        }
        return true;
    }

    private void saveProjectArchiveRecord(String subjectId, String projectId, String uploadPath) {
        String currentUrl = getSubjectArchiveUrl(projectId, subjectId);
        if (currentUrl == null) {
            String url = excelConfig.getArchiveUrlPrefix() + uploadPath;
            daoFactory.getManagerDao().execute("insert into report_archive(project_id,subject_id,last_generate,archive_url) " +
                    "values(?,?,current_timestamp,?)", projectId, subjectId, url);
        } else {
            daoFactory.getManagerDao().execute("update report_archive set archive_url = ?," +
                    "last_generate = current_timestamp where project_id = ? and subject_id =?", currentUrl, projectId, subjectId);
        }
    }

    private String uploadZipArchive(String projectId, File tempFile, final String uploadFileName) {
        String uploadPath = "report-archives/" + projectId + "/" + uploadFileName;
        ossFileClient.deleteFile(uploadPath);    // 文件上传完成到可用期间存在延时，因此删除旧文件，以免旧文件被误下载
        ossFileClient.uploadFile(tempFile, uploadPath);
        return uploadPath;
    }

    private File createZipArchive(String archiveRoot) throws ReportArchiveException {
        try {
            File tempFile = File.createTempFile("xz-report-archive", ".zip");
            ZipFileCreator creator = new ZipFileCreator(tempFile);

            FileUtils.iterateFiles(new File(archiveRoot),
                    (file, path) -> {
                        try {
                            creator.putEntry(path, FileUtils.readFileBytes(file));
                        } catch (IOException e) {
                            throw new ReportArchiveException(e);
                        }
                    });

            creator.close();
            return tempFile;
        } catch (ReportArchiveException e) {
            throw e;
        } catch (IOException e) {
            throw new ReportArchiveException(e);
        }
    }

    public void startSubjectArchive(String projectId, String subjectId) {
        Runnable runnable = () -> startSubjectArchive0(projectId, subjectId);
        startRunnable(projectId, runnable);
    }

    private void startSubjectArchive0(String projectId, String subjectId) {
        LOG.info("项目 " + projectId + " 的科目 " + subjectId + " 开始打包报表...");

        try {
            //查该项目该科目是否有过Basic统计记录
            if (!hasBasicAggregate(projectId)) {
                //如果需要重新进行basic统计,则需要删除之前的url记录
                deleteLastUrl(projectId, subjectId);
                runBasicAggregate(projectId);
            }

            //上次生成Excel之后无统计记录直接跳过
            boolean generate = hasGenerateAfterBasicAggr(projectId);
            if (generate) {
                LOG.info("项目{}上次统计之后并无生成Excel记录,并生成本次Excel报表............",projectId);
                excelReportManager.generateReports(projectId, false, true);
            }

            if (generate) {
                LOG.info("生成新的Excel报表,需要重新打包上传....");
                String subjectName = SubjectService.getSubjectName(subjectId);
                String excelPath = excelConfig.getSavePath();
                String archiveRoot = ExcelReportManager.getSaveFilePath(projectId, excelPath, "单科报表/" + subjectName);
                Row row = daoFactory.getManagerDao().queryFirst("select * from project where id = ?", projectId);

                String fileName = row.getString("name") + "_" + subjectName + ".zip";

                File tempFile = createZipArchive(archiveRoot);
                String uploadPath = uploadZipArchive(projectId, tempFile, fileName);
                saveProjectArchiveRecord(subjectId, projectId, uploadPath);
            }

        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            LOG.info("项目 " + projectId + " 的科目 " + subjectId + " 报表打包结束。");
        }
    }

    private void deleteLastUrl(String projectId, String subjectId) {
        DAO managerDao = daoFactory.getManagerDao();
        if (subjectId == null) {
            managerDao.execute("delete from report_archive where project_id = ? and subject_id = ?", projectId, "000");
        } else {
            managerDao.execute("delete from report_archive where project_id = ? and subject_id = ?", projectId, subjectId);
        }
    }

    //执行Basic统计
    private void runBasicAggregate(String projectId) {
        AggregateParameter parameter = new AggregateParameter();
        parameter.setAggregateType(AggregateType.Basic);
        parameter.setProjectId(projectId);
        parameter.setIgnoreStatus(true);
        aggregateService.runAggregate(parameter);
    }

    public ArchiveStatus getProjectArchiveStatus(String projectId) {
        return projectService.findProject(projectId).getStatus()
                .equals(ProjectStatus.Ready.name()) ? ArchiveStatus.Ready : ArchiveStatus.Running;
    }

    public String getSubjectArchiveUrl(String projectId, String subjectId) {
        Row row = daoFactory.getManagerDao().queryFirst(
                "select archive_url from report_archive where project_id=? and subject_id=?", projectId, subjectId);
        return row == null ? null : row.getString("archive_url");
    }
}
