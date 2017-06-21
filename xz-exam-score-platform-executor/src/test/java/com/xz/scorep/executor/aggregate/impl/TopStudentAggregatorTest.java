package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-06-13.
 */
public class TopStudentAggregatorTest extends BaseTest {

    private static final String SQL = "set @ss=(\n" +
            "select (\n" +
            "\tselect b.score from \n" +
            "\t\t(select (@row := @row +1) num,s.* from score_project s,(select @row :=0) a ORDER BY score desc) b \n" +
            "\t\twhere b.num = (select floor(COUNT(1) * 0.05) from score_project)\n" +
            "\t) abc\n" +
            ");\n" +
            "select * from score_project  where score >  @ss or score =  @ss  order by score desc";


    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;

    @Autowired
    TopStudentAggregator aggregator;

    private static final String PROID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter(PROID));
    }

    @Test
    public void test() {
        DAO projectDao = daoFactory.getProjectDao(PROID);
//        ReportConfig reportConfig = reportConfigService.queryReportConfig(PROID);
//        double rate = reportConfig.getTopStudentRate();
//        String replace = SQL.replace("{{rate}}", String.valueOf(rate));
//        Row row = projectDao.queryFirst(replace);
//
//        String sql = "select * from score_project  where score > {{score}} or score = {{score}} order by score desc";
//        List<Row> rows = projectDao.query(sql.replace("{{score}}", row.getString("score")));
        List<Row> rows = projectDao.query(SQL);
        System.out.println(rows.size());

    }
}