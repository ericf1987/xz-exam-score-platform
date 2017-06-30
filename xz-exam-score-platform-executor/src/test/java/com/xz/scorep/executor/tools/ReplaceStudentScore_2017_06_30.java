package com.xz.scorep.executor.tools;

import com.alibaba.druid.pool.DruidDataSource;
import com.hyd.dao.DAO;
import com.hyd.dao.DataSources;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.lang.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 由于学生试卷错位，导致一些试卷背面与正面不是同一个学生的，现在需要将分数对应的学生纠正。
 *
 * @author yidin
 */
public class ReplaceStudentScore_2017_06_30 {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceStudentScore_2017_06_30.class);

    public static final String PROJECT_ID = "430100-85ebb706b7fd4554a8085bd462aba646";

    public static final List<String> QUEST_IDS = Arrays.asList("100010237-20", "100010237-21", "100010237-22");

    public static final String FILES_FOLDER = "E:/corrections";

    static {
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "2346");
    }

    public static void main(String[] args) {
        DataSources dataSources = initDataSources();
        DAO dao = dataSources.getDAO("default");

        Map<String, String> examNoMap = readExamNoMap(dao);
        Map<String, String> correctionMap = readCorrectionMap();
        List<Correction> correctionSequence = buildCorrectionSequence(correctionMap);

        QUEST_IDS.forEach(questId -> {
            String tableName = "score_" + questId;
            List<Row> correctedRows = new ArrayList<>();

            Map<String, Row> rowMap = dao.query("select * from `" + tableName + "`")
                    .stream().collect(Collectors.toMap(row -> row.getString("student_id"), row-> row));

            for (Correction correction : correctionSequence) {
                LOG.info("Correcting " + correction);
                String fakeStudentId = examNoMap.get(correction.getFakeStudent());
                String realStudentId = examNoMap.get(correction.getRealStudent());

                if (rowMap.containsKey(fakeStudentId)) {
                    dao.execute("delete from `" + tableName + "` where student_id=?", fakeStudentId);
                    Row row = rowMap.get(fakeStudentId);
                    row.put("studentId", realStudentId);
                    correctedRows.add(row);
                }
            }

            LOG.info("Inserting corrected rows to " + tableName);
            dao.insert(correctedRows, tableName);
        });

    }

    private static Map<String, String> readExamNoMap(DAO dao) {
        Map<String, String> result = new HashMap<>();

        dao.query("select school_exam_no,id from student").forEach(row ->
                result.put(row.getString("school_exam_no"), row.getString("id")));

        return result;
    }

    private static Map<String, String> readCorrectionMap() {
        Map<String, String> correctionMap = new HashMap<>();
        File dir = new File(FILES_FOLDER);

        Optional.ofNullable(dir.listFiles()).ifPresent(files -> {
            LOG.info(files.length + " files found.");
            for (File file : files) {
                try {
                    LOG.info("Reading file " + file.getName() + "...");
                    FileUtils.readLines(file).forEach(line -> parseLine(correctionMap, line));
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        });
        return correctionMap;
    }

    private static void parseLine(Map<String, String> map, String line) {
        line = line.trim();
        String[] split = line.split("\\s+");

        if (split.length == 2) {
            map.put(split[0], split[1]);
        }
    }

    //////////////////////////////////////////////////////////////

    private static List<Correction> buildCorrectionSequence(Map<String, String> correctionMap) {
        List<Correction> correctionSequence = new ArrayList<>();
        correctionMap.forEach((fake, real) -> correctionSequence.add(new Correction(fake, real)));
        return correctionSequence;
    }

    private static DataSources initDataSources() {
        String username = StringUtil.substring(PROJECT_ID, 0, 32);

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://10.10.22.154:3306/" + PROJECT_ID + "?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false");
        dataSource.setUsername(username);
        dataSource.setPassword(username);

        DataSources dataSources = new DataSources();
        dataSources.setDataSource("default", dataSource);
        return dataSources;
    }

    private static class Correction {

        private String fakeStudent;

        private String realStudent;

        public Correction(String fakeStudent, String realStudent) {
            this.fakeStudent = fakeStudent;
            this.realStudent = realStudent;
        }

        public String getFakeStudent() {
            return fakeStudent;
        }

        public void setFakeStudent(String fakeStudent) {
            this.fakeStudent = fakeStudent;
        }

        public String getRealStudent() {
            return realStudent;
        }

        public void setRealStudent(String realStudent) {
            this.realStudent = realStudent;
        }

        @Override
        public String toString() {
            return "Correction{" +
                    "fakeStudent='" + fakeStudent + '\'' +
                    ", realStudent='" + realStudent + '\'' +
                    '}';
        }
    }
}
