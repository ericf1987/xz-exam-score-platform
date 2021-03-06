package com.xz.scorep.executor.aggregate;

import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.importproject.ImportProjectParameters;
import com.xz.scorep.executor.importproject.ImportProjectService;
import com.xz.scorep.executor.project.BackupAggregateDataService;
import com.xz.scorep.executor.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

@Service
public class AggregateService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateService.class);

    @Autowired
    private AggregationExecutorService executorService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private ImportProjectService importProjectService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BackupAggregateDataService backupService;

    private Map<String, Aggregator> aggregatorMap = new HashMap<>();

    /**
     * 注册 Aggregator 实例
     *
     * @param aggregator Aggregator 实例
     */
    void registerAggregator(Aggregator aggregator) {
        this.aggregatorMap.put(aggregator.getAggrName(), aggregator);
    }

    //////////////////////////////////////////////////////////////

    /**
     * 执行统计（异步）
     *
     * @param parameter 统计参数
     */
    public void runAggregateAsync(AggregateParameter parameter) {
        Runnable runnable = () -> {
            try {
                runAggregate(parameter);
            } catch (Exception e) {
                LOG.error("项目 " + parameter.getProjectId() + " 正在统计当中，跳过重复的统计请求");
            }
        };
        executorService.submit(runnable);
    }

    /**
     * 执行统计
     *
     * @param parameter 统计参数
     */
    public void runAggregate(AggregateParameter parameter) {
        AggregateType aggregateType = parameter.getAggregateType();

        runAggregate(parameter, aggregator ->
                aggregateType == AggregateType.Complete || aggregator.isOfType(aggregateType));
    }

    /**
     * 执行一个特定的统计项
     *
     * @param projectId 项目ID
     * @param aggrName  统计项名称，即 Aggregator 类的前缀
     */
    public void runAggregate(String projectId, String aggrName) {
        Aggregator aggregator = this.aggregatorMap.get(aggrName);
        runAggregate(new Aggregation(projectId), aggregator);
    }

    /**
     * 执行满足条件的统计项
     *
     * @param parameter 统计参数
     * @param condition 对哪些指标做统计
     */
    public void runAggregate(final AggregateParameter parameter, Predicate<Aggregator> condition) {

        String projectId = parameter.getProjectId();

        if (aggregationService.getAggregateByStatus(projectId, "Running") != null) {
            throw new IllegalStateException("项目 " + projectId + "正在统计中...");
        }

        Aggregation aggregation = new Aggregation(projectId);

        try {

            // 添加本次统计记录
            createAggregationRecord(aggregation, parameter);

            // 根据需要导入数据
            importData(parameter);

            // 对要求统计的指标执行统计
            runAggregators(condition, parameter);

        } catch (Exception e) {
            LOG.error("项目 " + projectId + " 统计失败", e);
        } finally {
            aggregation.setStatus(AggregateStatus.Finished);
            aggregation.setEndTime(new Date());

            // 更新本次统计记录
            aggregationService.endAggregation(
                    aggregation.getId(), aggregation.getStatus(), aggregation.getEndTime());

            LOG.info("项目 " + projectId + " 统计完成，耗时 " + aggregation.duration() + " ms");
        }
    }

    private void createAggregationRecord(Aggregation aggregation, AggregateParameter parameter) {
        //添加本次统计科目信息
        aggregation.setSubjectId(String.join(",", parameter.getSubjects()));
        aggregation.setAggrType(parameter.getAggregateType());
        aggregation.setStatus(AggregateStatus.Running);
        aggregation.setStartTime(new Date());
        aggregationService.insertAggregation(aggregation);
    }

    private void runAggregators(Predicate<Aggregator> condition, AggregateParameter parameter) throws Exception {

        String projectId = parameter.getProjectId();

        // 如果项目当前是空闲状态，则成功修改为正在统计，否则不能开始执行
        if (!parameter.isIgnoreStatus() && !projectService.updateProjectStatus(
                projectId, ProjectStatus.Ready, ProjectStatus.Aggregating)) {
            throw new IllegalStateException("项目 " + projectId + " 正忙，无法进行统计");
        }

        try {
            List<Aggregator> aggregators = new ArrayList<>(this.aggregatorMap.values());
            aggregators.removeIf(condition.negate());
            aggregators.sort(Comparator.comparingInt(Aggregator::getAggregateOrder));

            LOG.info("== 统计项顺序 ==");
            aggregators.forEach(aggregator -> {
                LOG.info(String.format("%5d %s", aggregator.getAggregateOrder(), aggregator.getAggrName()));
            });
            LOG.info("=================");

            for (Aggregator aggregator : aggregators) {
                aggregator.aggregate(parameter);
            }

            //此处不抛异常则表明统计完成
            List<String> subjects = parameter.getSubjects();
            LOG.info("项目ID {} ,subjects {}, 统计类型 {}",projectId,subjects,parameter.getAggregateType().name());
            if (!subjects.isEmpty() && parameter.getAggregateType() == AggregateType.Quick) {
                for (String subjectId : subjects) {
                    if (!"".equals(subjectId)) {
                        backupService.copyOriginDataToBackupDataBase(projectId, subjectId);
                    }
                }
            }

        } finally {
            // 如果成功开始统计，则在结束后恢复项目状态
            LOG.info("项目ID {} 在更新状态.....",projectId);
            projectService.updateProjectStatus(projectId, ProjectStatus.Aggregating, ProjectStatus.Ready);
            LOG.info("项目ID {} 更新状态完成.....",projectId);
        }
    }

    private void importData(AggregateParameter parameter) {
        String projectId = parameter.getProjectId();

        // 导入项目信息
        if (parameter.isImportProject()) {
            importProjectService.importProject(ImportProjectParameters.importAllButScore(projectId));
        }

        // 导入项目分数
        if (parameter.isImportScore()) {
            importProjectService.importProject(ImportProjectParameters.importScoreOnly(projectId));
        }
    }

    // 执行单个统计项
    private void runAggregate(Aggregation aggregation, Aggregator aggregator) {
        if (aggregator != null) {

            String projectId = aggregation.getProjectId();
            String aggrName = aggregator.getAggrName();

            try {
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计开始...");
                aggregator.aggregate(new AggregateParameter(projectId));
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计完毕。");
            } catch (Exception e) {
                LOG.error("项目 " + projectId + " 的 " + aggrName + " 统计失败", e);
            }
        }
    }

}
