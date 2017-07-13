package com.xz.scorep.executor.aggregate;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AggregationService {

    public static final String QUERY_AGGREGATION_STATUS = "select * from aggregation where aggr_type = 'Quick' and project_id = '{{projectId}}' {{condition}} order by start_time desc";

    private static final Logger LOG = LoggerFactory.getLogger(AggregationService.class);

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ProjectService projectService;

    public void insertAggregation(Aggregation aggregation) {
        DAO managerDao = this.daoFactory.getManagerDao();
        managerDao.delete(aggregation, "aggregation");
        managerDao.insert(aggregation, "aggregation");
    }

    public void endAggregation(String aggregationId, AggregateStatus status, Date endTime) {
        this.daoFactory.getManagerDao().execute(
                SQL.Update("aggregation")
                        .Set("status", status.name()).Set("end_time", endTime)
                        .Where("id=?", aggregationId));
    }

    public Row getAggregateStatus(String projectId) {
        String sql = "select * from aggregation where project_id = ? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId);
    }

    public Row getAggregateByStatus(String projectId, String status) {
        String sql = "select * from aggregation where project_id = ? and status = ? order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, status);
    }


    public Row getAggregateStatus(String projectId, String subjectId) {
        String sql = "select * from aggregation where project_id = ? and subject_id =? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, subjectId);
    }


    public Row getAggregateStatus(String projectId, AggregateType aggrType) {
        String sql = "select * from aggregation where project_id = ? and aggr_type =?  and status = 'Finished' order by end_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, aggrType.name());
    }

    public Row getAggregateStatus(String projectId, AggregateType aggrType, String subjectId) {
        String sql = "select * from aggregation where project_id = ? and aggr_type =? and subject_id =? and status = 'Finished' order by start_time desc";
        return this.daoFactory.getManagerDao().queryFirst(sql, projectId, aggrType.name(), subjectId);
    }


    public Result checkProjectStatus(String projectId, String subjectId) {
        DAO managerDao = daoFactory.getManagerDao();
        ExamProject project = projectService.findProject(projectId);
        if (ProjectStatus.Importing.name().equals(project.getStatus())) {
            return Result.fail(1, "正在统计项目,请稍后再试");
        }
        String condition = StringUtil.isEmpty(subjectId) ? "" : " and subject_id = '" + subjectId + "'";

        Row row = managerDao.queryFirst(QUERY_AGGREGATION_STATUS
                .replace("{{projectId}}", projectId)
                .replace("{{condition}}", condition));
        LOG.info("项目ID {} ,科目ID {} ，row {}", projectId, subjectId, row);
        if (row == null) {
            return Result.fail(1, "尚未找到项目,请确保项目已统计");
        }
        if (!AggregateStatus.Finished.name().equals(row.getString("status"))) {
            return Result.fail(1, "项目正在统计,请稍后再试");
        }
        return Result.success();
    }
}
