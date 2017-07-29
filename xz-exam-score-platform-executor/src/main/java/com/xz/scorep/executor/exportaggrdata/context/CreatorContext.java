package com.xz.scorep.executor.exportaggrdata.context;

import com.xz.ajiaedu.common.lang.CollectionUtils;
import com.xz.scorep.executor.exportaggrdata.bean.*;
import com.xz.scorep.executor.exportaggrdata.exception.CreatorException;
import com.xz.scorep.executor.exportaggrdata.packcreator.*;
import com.xz.scorep.executor.exportaggrdata.query.ScoreSegmentsQuery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        ENTRY_CREATORS.add(new MaxMinCreator());
        ENTRY_CREATORS.add(new OverAverageCreator());
        ENTRY_CREATORS.add(new QuestDeviationCreator());
        ENTRY_CREATORS.add(new ObjCorrectMapCreator());
        ENTRY_CREATORS.add(new QuestTypeScoreCreator());
        ENTRY_CREATORS.add(new QuestTypeScoreAverageCreator());
        ENTRY_CREATORS.add(new RankLevelCreator());
        ENTRY_CREATORS.add(new RankLevelMapCreator());
        ENTRY_CREATORS.add(new RankSegmentCreator());
        ENTRY_CREATORS.add(new ScoreLevelMapCreator());
        ENTRY_CREATORS.add(new TScoreCreator());
        ENTRY_CREATORS.add(new ScoreRateCreator());
        ENTRY_CREATORS.add(new StdDeviationCreator());
        ENTRY_CREATORS.add(new SubjectRateCreator());
        ENTRY_CREATORS.add(new TopAverageCreator());
        ENTRY_CREATORS.add(new TotalScoreCreator());
        ENTRY_CREATORS.add(new ScoreSegmentsCreator());
        ENTRY_CREATORS.add(new TopStudentListCreator());
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

    //题目题型得分
    private final List<Map<String, Object>> questTypeScores = new ArrayList<>();

    //题目题型得分平均分
    private final List<QuestTypeScoreAverage> questTypeScoreAverages = new ArrayList<>();

    //排名等级
    private final List<RankLevel> rankLevels = new ArrayList<>();

    //排名等第
    private final List<RankLevelMap> rankLevelMaps = new ArrayList<>();

    //排名分段
    private final List<RankSegment> rankSegments = new ArrayList<>();

    //分数等级
    private final List<ScoreLevelMap> scoreLevelMaps = new ArrayList<>();

    //T分值
    private final List<TScore> tScores = new ArrayList<>();

    //得分率
    public final List<ScoreRate> scoreRates = new ArrayList<>();

    //标准差
    public final List<StdDeviation> stdDeviations = new ArrayList<>();

    //科目贡献度
    public final List<SubjectRate> subjectRates = new ArrayList<>();

    //高分段平均分
    private final List<TopAverage> topAverages = new ArrayList<>();

    //totalScore 总分
    private final List<Map<String, Object>> totalScores = new ArrayList<>();

    //scoreSegments
    private final List<ScoreSegment> scoreSegments = new ArrayList<>();

    //topStudentList
    private final List<Map<String,Object>> topStudentLists = new ArrayList<>();

    public List<AllPassOrFail> getAllPassOrFails() {
        return allPassOrFails;
    }

    public List<Average> getAverages() {
        return averages;
    }

    public List<MaxMin> getMaxMins() {
        return maxMins;
    }

    public List<ObjCorrectMap> getObjCorrectMaps() {
        return objCorrectMaps;
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

    public List<RankSegment> getRankSegments() {
        return rankSegments;
    }

    public List<ScoreLevelMap> getScoreLevelMaps() {
        return scoreLevelMaps;
    }

    public List<ScoreRate> getScoreRates() {
        return scoreRates;
    }

    public List<StdDeviation> getStdDeviations() {
        return stdDeviations;
    }

    public List<TScore> gettScores() {
        return tScores;
    }

    public List<SubjectRate> getSubjectRates() {
        return subjectRates;
    }

    public List<QuestTypeScoreAverage> getQuestTypeScoreAverages() {
        return questTypeScoreAverages;
    }

    public List<TopAverage> getTopAverages() {
        return topAverages;
    }

    public List<Map<String, Object>> getTotalScores() {
        return totalScores;
    }

    public List<ScoreSegment> getScoreSegments() {
        return scoreSegments;
    }

    public List<Map<String, Object>> getTopStudentLists() {
        return topStudentLists;
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
