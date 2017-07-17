package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.Row;
import com.sun.tools.javac.util.List;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author by fengye on 2017/7/17.
 */
@Component
public class AverageQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    public static final String SUBJECT_DATA = "SELECT AVG(score), stu.{{range_id}} range_id \n" +
            "from score_subject_{{subject_id}} score, student stu\n" +
            "WHERE score.`student_id` = stu.`id`\n" +
            "group by range_id";

    public List<Row> queryData(String projectId){

        return null;
    }
}
