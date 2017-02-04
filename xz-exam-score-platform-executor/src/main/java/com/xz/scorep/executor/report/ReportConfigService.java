package com.xz.scorep.executor.report;

import com.alibaba.fastjson.JSON;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportConfigService {

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    public void saveReportConfig(ReportConfig reportConfig) {
        getManagerDBIHandle().runHandle(handle -> {
            String sql = "INSERT INTO `report_config`\n" +
                    "(`project_id`\n" +
                    ",`combine_category_subjects`\n" +
                    ",`separate_category_subjects`\n" +
                    ",`college_entry_level_enabled`\n" +
                    ",`rank_levels`\n" +
                    ",`rank_segment_count`\n" +
                    ",`score_levels`\n" +
                    ",`rank_level_combines`\n" +
                    ",`top_student_rate`\n" +
                    ",`high_score_rate`\n" +
                    ",`college_entry_level`\n" +
                    ",`entry_level_stat_type`\n" +
                    ",`share_school_report`) values (" +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?)";

            handle.insert(sql, reportConfig.getProjectId(),
                    Boolean.toString(reportConfig.isCombineCategorySubjects()),
                    Boolean.toString(reportConfig.isSeparateCombine()),
                    Boolean.toString(reportConfig.isEntryLevelEnable()),
                    JSON.toJSONString(reportConfig.getRankLevels()),
                    reportConfig.getRankSegmentCount(),
                    JSON.toJSONString(reportConfig.getScoreLevels()),
                    JSON.toJSONString(reportConfig.getRankLevelCombines()),
                    reportConfig.getTopStudentRate(),
                    reportConfig.getHighScoreRate(),
                    JSON.toJSONString(reportConfig.getCollegeEntryLevel()),
                    reportConfig.getEntryLevelStatType(),
                    Boolean.toString(reportConfig.isShareSchoolReport()));
        });
    }

    private DBIHandle getManagerDBIHandle() {
        return dbiHandleFactoryManager.getDefaultDbiHandleFactory().getManagerDBIHandle();
    }
}