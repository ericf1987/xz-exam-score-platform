package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Point;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.Average;
import com.xz.scorep.executor.project.PointService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/17.
 */
@Component
public class AverageScoreQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    PointService pointService;

    @Autowired
    SubjectService subjectService;

    public static final String SUBJECT_DATA = "select '{{range_name}}' range_name,student.{{range_id}} range_id,\n" +
            "'{{target_name}}' target_name,'{{subjectId}}' target_id,\n" +
            "avg(score) average from `score_subject_{{subjectId}}` score,student \n" +
            "where score.student_id = student.id\n" +
            "GROUP BY student.{{range_id}}";

    public static final String POINT_DATA = "select '{{range_name}}' range_name,student.{{range_id}} range_id," +
            "'{{target_name}}' target_name,score.point_id target_id,\n" +
            "avg(score.total_score) average from `score_point` score,student \n" +
            "where score.student_id = student.id\n" +
            "and score.point_id = '{{point_id}}'\n" +
            "GROUP BY student.{{range_id}};";

    public List<Average> queryData(String projectId) {
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);

//        List<Row> subjectRows = subjects.stream()
//                .map(subject -> querySubjectData(projectId, subject))
//                .flatMap(x -> x.stream())
//                .collect(Collectors.toList());

        List<String> subjectSqls = generateSubjectSql(subjects);
        List<String> pointSqls = generatePointSql(pointService.listPoints(projectId));

        List<AverageTask> tasks = new ArrayList<>();
        AverageTask subjectTask = new AverageTask(daoFactory.getProjectDao(projectId), subjectSqls);
        AverageTask pointTask = new AverageTask(daoFactory.getProjectDao(projectId), pointSqls);
        tasks.add(subjectTask);
        tasks.add(pointTask);

        List<Row> list = new ArrayList<>();
        for (AverageTask task : tasks) {
            try {
                task.start();
                task.join();
                list.addAll(task.getResult());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


//        List<Row> pointRows = pointService.listPoints(projectId).stream()
//                .map(point -> queryPointData(projectId, point))
//                .flatMap(x -> x.stream())
//                .collect(Collectors.toList());


//        List<Row> rows = addAll(subjectRows);
        List<Average> result = list.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        return result;
    }

    private List<String> generatePointSql(List<Point> points) {
        List<String> result = new ArrayList<>();
        points.forEach(point -> {
            String tem = POINT_DATA.replace("{{target_name}}", "point").replace("{{point_id}}", point.getPointId());

            result.add(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
            result.add(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
            result.add(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        });
        return result;
    }

    private List<String> generateSubjectSql(List<ExamSubject> subjects) {
        List<String> result = new ArrayList<>();
        subjects.forEach(subject -> {
            String name = subject.getId().length() > 3 ? "subjectCombination" : "subject";
            String tem = SUBJECT_DATA.replace("{{subjectId}}", subject.getId()).replace("{{target_name}}", name);
            result.add(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
            result.add(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
            result.add(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        });
        return result;
    }

    private List<Row> addAll(List<Row>... rows) {
        List<Row> result = new ArrayList<>();
        for (List<Row> list : rows) {
            result.addAll(list);
        }
        return result;
    }

    private List<Row> queryPointData(String projectId, Point point) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tem = POINT_DATA.replace("{{target_name}}", "point").replace("{{point_id}}", point.getPointId());

        List<Row> classRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
        List<Row> schoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
        List<Row> provinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        List<Row> result = new ArrayList<>();
        result.addAll(classRows);
        result.addAll(schoolRows);
        result.addAll(provinceRows);
        return result;

    }


    private List<Row> querySubjectData(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String name = subject.getId().length() > 3 ? "subjectCombination" : "subject";
        String tem = SUBJECT_DATA.replace("{{subjectId}}", subject.getId()).replace("{{target_name}}", name);
        List<Row> classRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
        List<Row> schoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
        List<Row> provinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        List<Row> result = new ArrayList<>();
        result.addAll(classRows);
        result.addAll(schoolRows);
        result.addAll(provinceRows);
        return result;
    }

    private Average pakObj(Row row, String projectId) {
        Average average = new Average();
        average.setAverage(row.getDouble("average", 0));
        average.setRange(new Range(row.getString("range_name"), row.getString("range_id")));
        average.setTarget(new Target(row.getString("target_name"), row.getString("target_id")));
        average.setProject(projectId);

        average.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return average;
    }
}

class AverageTask extends Thread {

    private List<String> sqlList;

    private DAO projectDao;

    private List<Row> result = new ArrayList<>();

    public AverageTask(DAO projectDao, List<String> sqlList) {
        this.sqlList = sqlList;
        this.projectDao = projectDao;
    }

    @Override
    public void run() {
        for (String sql : this.sqlList) {
            List<Row> rows = projectDao.query(sql);
            this.result.addAll(rows);
        }
    }


    public List<Row> getResult() {
        return result;
    }
}