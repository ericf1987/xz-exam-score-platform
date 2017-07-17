package com.xz.scorep.executor.exportaggrdata.service;

import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.scorep.executor.exportaggrdata.bean.AllPassOrFail;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import com.xz.scorep.executor.exportaggrdata.packcreator.AllPassOrFailCreator;
import com.xz.scorep.executor.exportaggrdata.query.AllPassOrFailQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author by fengye on 2017/7/17.
 */
@Service
public class AggregationDataExport {

    @Autowired
    AllPassOrFailQuery allPassOrFailQuery;

    @Autowired
    AllPassOrFailCreator allPassOrFailCreator;

    static final Logger LOG = LoggerFactory.getLogger(AggregationDataExport.class);

    public void exportData(String projectId) {

        // 本地文件路径
        String filePath = "F://aggregation-data-archives//" + UUID.randomUUID().toString() + ".zip";

        //创建文件压缩包
        createPack(projectId, filePath);

    }

    private void createPack(String projectId, String filePath) {
        CreatorContext context = new CreatorContext();

        //全科不及格/及格率数据
        List<AllPassOrFail> allPassOrFails = allPassOrFailQuery.queryObj(projectId);

        context.getAllPassOrFails().addAll(allPassOrFails);

        try {
            FileUtils.writeFile(context.createZipArchive(), new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
