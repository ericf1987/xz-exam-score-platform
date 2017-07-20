package com.xz.scorep.executor.exportaggrdata.context;

import com.xz.ajiaedu.common.lang.CollectionUtils;
import com.xz.scorep.executor.exportaggrdata.bean.*;
import com.xz.scorep.executor.exportaggrdata.exception.CreatorException;
import com.xz.scorep.executor.exportaggrdata.packcreator.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 统计数据文件包
 *
 * @author by fengye on 2017/7/17.
 */
public class CreatorContext {

    //定义多个统计表创建器
    private static final List<ScoreDataEntryCreator> ENTRY_CREATORS = new ArrayList<>();

    static {
        ENTRY_CREATORS.add(new AllPassOrFailCreator());
        ENTRY_CREATORS.add(new AverageCreator());
        ENTRY_CREATORS.add(new RankLevelCreator());
        ENTRY_CREATORS.add(new RankLevelMapCreator());
        ENTRY_CREATORS.add(new MaxMinCreator());
        ENTRY_CREATORS.add(new ObjCorrectMapCreator());
        ENTRY_CREATORS.add(new OverAverageCreator());
        ENTRY_CREATORS.add(new QuestDeviationCreator());
        ENTRY_CREATORS.add(new QuestTypeScoreCreator());
    }

    //全科及格率
    private final List<AllPassOrFail> allPassOrFails = new ArrayList<>();

    //平均分
    private final List<Average> averages = new ArrayList<>();

    //最大最小分
    private final List<MaxMin> maxMins = new ArrayList<>();

    //超出平均分
    private final List<OverAverage> overAverages = new ArrayList<>();

    //题目区分度
    private final List<QuestDeviation> questDeviations = new ArrayList<>();

    //客观题正确率
    private final List<ObjCorrectMap> objCorrectMaps = new ArrayList<>();

    //
    private final List<Map<String, Object>> questTypeScores = new ArrayList<>();

    //排名等级
    private final List<RankLevel> rankLevels = new ArrayList<>();

    //排名等第
    private final List<RankLevelMap> rankLevelMaps = new ArrayList<>();

    public List<AllPassOrFail> getAllPassOrFails() {
        return allPassOrFails;
    }

    public List<Average> getAverages() {
        return averages;
    }

    public List<ObjCorrectMap> getObjCorrectMaps() {
        return objCorrectMaps;
    }

    public List<MaxMin> getMaxMins() {
        return maxMins;
    }

    public List<OverAverage> getOverAverages() {
        return overAverages;
    }

    public List<QuestDeviation> getQuestDeviations() {
        return questDeviations;
    }

    public List<Map<String, Object>> getQuestTypeScores() {
        return questTypeScores;
    }

    public List<RankLevel> getRankLevels() {
        return rankLevels;
    }

    public List<RankLevelMap> getRankLevelMaps() {
        return rankLevelMaps;
    }

    public byte[] createZipArchive() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ZipOutputStream e = new ZipOutputStream(baos);
            Throwable var3 = null;

            try {
                Iterator var4 = ENTRY_CREATORS.iterator();

                while (var4.hasNext()) {
                    ScoreDataEntryCreator entryCreator = (ScoreDataEntryCreator) var4.next();
                    this.createEntries(e, entryCreator);
                }
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (var3 != null) {
                    try {
                        e.close();
                    } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                    }
                } else {
                    e.close();
                }

            }
        } catch (IOException var16) {
            throw new CreatorException("Error creating zip archive", var16);
        }

        return baos.toByteArray();
    }

    //将creator的entry数据写入文件，并打包
    private void createEntries(ZipOutputStream zos, ScoreDataEntryCreator entryCreator) throws IOException {
        List entryDatas = entryCreator.createEntries(this);
        if (!CollectionUtils.isEmpty(entryDatas)) {

            for (Object entryData1 : entryDatas) {
                EntryData entryData = (EntryData) entryData1;
                ZipEntry entry = new ZipEntry(entryData.getName());
                zos.putNextEntry(entry);
                zos.write(entryData.getContent());
                zos.closeEntry();
            }

        }
    }
}
