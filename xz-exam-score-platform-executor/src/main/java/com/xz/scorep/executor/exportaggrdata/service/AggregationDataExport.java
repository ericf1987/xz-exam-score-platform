package com.xz.scorep.executor.exportaggrdata.service;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.api.server.mongoaggr.NotifyImportMysqlDump;
import com.xz.scorep.executor.config.JsonConfig;
import com.xz.scorep.executor.exportaggrdata.bean.*;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import com.xz.scorep.executor.exportaggrdata.packcreator.AllPassOrFailCreator;
import com.xz.scorep.executor.exportaggrdata.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
    ObjCorrectMapQuery objCorrectMapQuery;

    @Autowired
    OverAverageQuery overAverageQuery;

    @Autowired
    QuestTypeScoreQuery questTypeScoreQuery;

    @Autowired
    AllPassOrFailCreator allPassOrFailCreator;

    @Autowired
    RankLevelQuery rankLevelQuery;

    @Autowired
    RankLevelMapQuery rankLevelMapQuery;

    @Autowired
    RankSegmentQuery rankSegmentQuery;

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
        //平均分
        List<Average> averages = averageScoreQuery.queryData(projectId);
        //最大最小分
        List<MaxMin> minMaxes = minMaxQuery.queryData(projectId);
        //客观题正确率
        List<ObjCorrectMap> correctMaps = objCorrectMapQuery.queryData(projectId);
        //超出平均分
        List<OverAverage> overAverages = overAverageQuery.queryData(projectId);

        List<Map<String, Object>> questTypeScoreMaps = questTypeScoreQuery.queryData(projectId);
        //等级排名
        List<RankLevel> rankLevels = rankLevelQuery.queryObj(projectId);
        //排名等第
        List<RankLevelMap> rankLevelMaps = rankLevelMapQuery.queryObj(projectId);
        //排名分段
        List<RankSegment> rankSegments = rankSegmentQuery.queryObj(projectId);


        context.getAllPassOrFails().addAll(allPassOrFails);
        context.getAverages().addAll(averages);
        context.getMaxMins().addAll(minMaxes);
        context.getObjCorrectMaps().addAll(correctMaps);
        context.getOverAverages().addAll(overAverages);
        context.getQuestTypeScores().addAll(questTypeScoreMaps);
        context.getRankLevels().addAll(rankLevels);
        context.getRankLevelMaps().addAll(rankLevelMaps);
        context.getRankSegments().addAll(rankSegments);

        try {
            FileUtils.writeFile(context.createZipArchive(), new File(filePath));
            LOG.info("文件写入成功");
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
