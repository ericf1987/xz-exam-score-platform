package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AggragateOrder(4)
@Component
public class RankAggregator extends Aggregator {

    public static final String RANGE_CLASS_TEMPLATE = "where student_id in(" +
        "    select student.id from student where student.class_id='{{class}}'\n" +
        ")";

    public static final String RANGE_SCHOOL_TEMPLATE = "where student_id in(" +
        "    select student.id from student,class where student.class_id=class.id and class.school_id='{{school}}'\n" +
        ")";

    public static final String INSERT_TEMPLATE = "insert into {{rank_table}}\n" +
            "select student_id, '{{subject}}' as subject_id, `rank` from (\n" +
            "  select \n" +
            "    student_id, score,\n" +
            "    @prev := @curr,\n" +
            "    @curr := score,\n" +
            "    @rank := IF(@prev = @curr, @rank, @rank+@step) as `rank`,\n" +
            "    @step := IF(@prev = @curr, (@step+1), 1) as step\n" +
            "  from \n" +
            "    score_project,\n" +
            "    (select @curr := null, @prev := null, @rank := 0, @step := 1) tmp1\n" +
            "  {{range_template}}\n" +
            "  order by score desc\n" +
            ") tmp2";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(String projectId) throws Exception {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        String sql = INSERT_TEMPLATE
                .replace("{{rank_table}}", "rank_province")
                .replace("{{subject}}", "000")
                .replace("{{range_template}}", "");

        projectDao.execute(sql);
    }
}
