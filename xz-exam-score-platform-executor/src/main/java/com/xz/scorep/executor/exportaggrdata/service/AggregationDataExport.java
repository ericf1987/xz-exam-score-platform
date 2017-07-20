package com.xz.scorep.executor.exportaggrdata.service;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.api.server.mongoaggr.NotifyImportMysqlDump;
import com.xz.scorep.executor.config.JsonConfig;
import com.xz.scorep.executor.exportaggrdata.bean.AllPassOrFail;
import com.xz.scorep.executor.exportaggrdata.bean.Average;
import com.xz.scorep.executor.exportaggrdata.bean.MaxMin;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import com.xz.scorep.executor.exportaggrdata.packcreator.AllPassOrFailCreator;
import com.xz.scorep.executor.exportaggrdata.query.AllPassOrFailQuery;
import com.xz.scorep.executor.exportaggrdata.query.AverageScoreQuery;
import com.xz.scorep.executor.exportaggrdata.query.MaxMinQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author by fengye on 2017/7/17.
 */
@Service
public class AggregationDataExport {

    @Autowired
    JsonConfig jsonConfig;

    @Autowired
    AllPassOrFailQuery allPassOrFailQuery;

    @Autowired
    AverageScoreQuery averageScoreQuery;

    @Autowired
    MaxMinQuery minMaxQuery;

    @Autowired
    AllPassOrFailCreator allPassOrFailCreator;

    @Autowired
    NotifyImportMysqlDump notifyImportMysqlDump;

    static final Logger LOG = LoggerFactory.getLogger(AggregationDataExport.class);

    public Result exportData(String projectId, boolean notifyImport) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        String date = format.format(Calendar.getInstance().getTime());

        // 本地文件路径
        String dumpPath = jsonConfig.getDumpPath();
        if (StringUtil.isEmpty(dumpPath)) {
            throw new IllegalStateException("json导出路径为空");
        }

        String filePath = StringUtil.joinPaths(dumpPath, date, projectId + ".zip");

        LOG.info("数据统计文件导出路径：{}", filePath);

        //创建文件压缩包
        createPack(projectId, filePath);

        //通知mongodb进行导入
        if (notifyImport) {
            notifyImport(projectId, filePath);
        }

        return Result.success();

    }

    private void createPack(String projectId, String filePath) {
        CreatorContext context = new CreatorContext();

        //全科不及格/及格率数据
        List<AllPassOrFail> allPassOrFails = allPassOrFailQuery.queryObj(projectId);
        List<Average> averages = averageScoreQuery.queryData(projectId);
        List<MaxMin> minMaxes = minMaxQuery.queryData(projectId);

        context.getAllPassOrFails().addAll(allPassOrFails);
        context.getAverages().addAll(averages);
        context.getMaxMins().addAll(minMaxes);

        try {
            FileUtils.writeFile(context.createZipArchive(), new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyImport(String projectId, String filePath) {
        notifyImportMysqlDump.execute(
                new Param().setParameter("projectId", projectId)
                        .setParameter("filePath", filePath)
        );
    }
}
