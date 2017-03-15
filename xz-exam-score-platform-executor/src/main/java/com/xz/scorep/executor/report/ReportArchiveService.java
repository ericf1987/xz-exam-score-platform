package com.xz.scorep.executor.report;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.aliyun.OSSFileClient;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.io.ZipFileCreator;
import com.xz.scorep.executor.config.ExcelConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelReportManager;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class ReportArchiveService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportArchiveService.class);

    private Set<String> runningArchives = new HashSet<>();

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ExcelConfig excelConfig;

    @Autowired
    private OSSFileClient ossFileClient;

    public void startProjectArchive(String projectId) {

        synchronized (this) {
            if (runningArchives.contains(projectId)) {
                LOG.info("项目 " + projectId + " 已经在打包全科报表。");
                return;
            }
        }

        LOG.info("开始给项目 " + projectId + " 打全科报表...");
        runningArchives.add(projectId);

        String excelPath = excelConfig.getSavePath();
        String archiveRoot = ExcelReportManager.getSaveFilePath(projectId, excelPath, "全科报表");

        File tempFile = createZipArchive(archiveRoot);
        String uploadPath = uploadZipArchive(projectId, tempFile, "all.zip");
        saveProjectArchiveRecord(projectId, uploadPath);

        LOG.info("项目 " + projectId + " 全科报表打包完毕。");
    }

    private void saveProjectArchiveRecord(String projectId, String uploadPath) {
        String currentUrl = getProjectArchiveUrl(projectId);
        if (currentUrl == null) {
            String url = excelConfig.getArchiveUrlPrefix() + uploadPath;
            daoFactory.getManagerDao().execute("insert into report_archive(project_id,last_generate,archive_url) " +
                    "values(?,current_timestamp,?)", projectId, url);
        }
    }

    private String uploadZipArchive(String projectId, File tempFile, final String uploadFileName) {
        String uploadPath = "report-archives/" + projectId + "/" + uploadFileName;
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

        synchronized (this) {
            if (runningArchives.contains(projectId + ":" + subjectId)) {
                LOG.info("项目 " + projectId + " 的科目 " + subjectId + " 已经在打包报表。");
                return;
            }
        }

        LOG.info("项目 " + projectId + " 的科目 " + subjectId + " 开始打包报表...");
        runningArchives.add(projectId + ":" + subjectId);

        String subjectName = SubjectService.getSubjectName(subjectId);
        String excelPath = excelConfig.getSavePath();
        String archiveRoot = ExcelReportManager.getSaveFilePath(projectId, excelPath, "单科报表/" + subjectName);

        File tempFile = createZipArchive(archiveRoot);
        String uploadPath = uploadZipArchive(projectId, tempFile, subjectId + ".zip");
        saveProjectArchiveRecord(projectId, uploadPath);
        LOG.info("项目 " + projectId + " 的科目 " + subjectId + " 报表打包完毕。");
    }

    public ArchiveStatus getProjectArchiveStatus(String projectId) {
        return runningArchives.contains(projectId) ? ArchiveStatus.Running : ArchiveStatus.Ready;
    }

    public ArchiveStatus getSubjectArchiveStatus(String projectId, String subjectId) {
        return runningArchives.contains(projectId + ":" + subjectId) ? ArchiveStatus.Running : ArchiveStatus.Ready;
    }

    public String getProjectArchiveUrl(String projectId) {
        Row row = daoFactory.getManagerDao().queryFirst(
                "select archive_url from report_archive where project_id=?", projectId);
        return row == null ? null : row.getString("archive_url");
    }

    public String getSubjectArchiveUrl(String projectId, String subjectId) {
        Row row = daoFactory.getManagerDao().queryFirst(
                "select archive_url from report_archive where project_id=? and subject_id=?", projectId, subjectId);
        return row == null ? null : row.getString("archive_url");
    }
}
