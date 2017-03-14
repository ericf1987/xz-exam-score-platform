package com.xz.scorep.executor.report;

import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ReportArchiveService {

    @Autowired
    private DAOFactory daoFactory;

    private Set<String> runningArchives = new HashSet<>();

    public void startProjectArchive(String projectId) {
        runningArchives.add(projectId);
    }

    public void startSubjectArchive(String projectId, String subjectId) {
        runningArchives.add(projectId + ":" + subjectId);
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
