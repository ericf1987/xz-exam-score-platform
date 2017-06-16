package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-06-13.
 */
public class SchoolTopStudentRateAggregatorTest extends BaseTest {

    private static final String SQL = "" +
            "select (\n" +
            "\tselect b.score from \n" +
            "\t\t(select (@row := @row +1) num,s.* from score_project s,(select @row :=0) a ORDER BY score desc) b \n" +
            "\t\twhere b.num = (select floor(COUNT(1) * {{rate}}) from score_project)\n" +
            "\t) score\n";


    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;

    @Autowired
    SchoolTopStudentRateAggregator aggregator;

    private static final String PROID = "430500-de127d78a5384d739e771a5ffa1b937f";

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter(PROID));
    }

    @Test
    public void test() {
        DAO projectDao = daoFactory.getProjectDao(PROID);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(PROID);
        double rate = reportConfig.getTopStudentRate();
        String replace = SQL.replace("{{rate}}", String.valueOf(rate));
        Row row = projectDao.queryFirst(replace);

        String sql = "select * from score_project  where score > {{score}} or score = {{score}} order by score desc";
        List<Row> rows = projectDao.query(sql.replace("{{score}}", row.getString("score")));
        System.out.println(rows.size());

    }
}