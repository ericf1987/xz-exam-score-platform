package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.ajiaedu.common.xml.XmlNode;
import com.xz.ajiaedu.common.xml.XmlNodeReader;
import com.xz.scorep.executor.bean.ProjectStatus;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.config.ExcelConfig;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.utils.AsyncCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * (description)
 * created at 16/05/30
 *
 * @author yiding_he
 */
@Component
public class ExcelReportManager implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelReportManager.class);

    private ThreadPoolExecutor executionPool;

    private XmlNode reportConfig;

    private ApplicationContext applicationContext;

    private Map<String, AsyncCounter> counterMap = new HashMap<>();  // 方便外部查询生成进度

    @Autowired
    private ExcelConfigParser excelConfigParser;

    @Autowired
    private ExcelConfig excelConfig;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private ReportCacheInitializer reportCacheInitializer;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        int poolSize = excelConfig.getPoolSize();
        this.executionPool = createThreadPool(poolSize);
        this.reportConfig = XmlNodeReader.read(getClass().getResourceAsStream("/report/config/report-config.xml"));
        if (StringUtil.isEmpty(excelConfig.getSavePath())) {
            throw new IllegalStateException("报表输出路径为空");
        }
    }

    public boolean isRunning(String projectId) {
        return projectService.findProject(projectId).getStatus()
                .equals(ProjectStatus.GeneratingReport.name());
    }

    public AsyncCounter getCounter(String projectId) {
        return counterMap.getOrDefault(projectId, null);
    }

    public void generateReports(final String projectId, boolean async, boolean ignoreStatus) {

        // 如果项目当前是空闲状态，则成功修改为正在生成 Excel，否则不能开始执行
        if (!ignoreStatus &&
                !projectService.updateProjectStatus(projectId, ProjectStatus.Ready, ProjectStatus.GeneratingReport)) {
            throw new IllegalStateException("项目 " + projectId + " 正忙，无法执行生成 Excel");
        }

        try {
            generateReports0(projectId, async);
        } finally {
            // 如果成功开始生成 Excel，则在结束后恢复项目状态
            projectService.updateProjectStatus(projectId, ProjectStatus.GeneratingReport, ProjectStatus.Ready);

            // 清理进度计数器
            counterMap.remove(projectId);

            // 清理项目对应的 report 缓存
            cacheFactory.removeReportCache(projectId);
        }
    }

    /**
     * 生成指定项目的所有报表文件
     *
     * @param projectId 项目ID
     * @param async     是否异步
     */
    private void generateReports0(final String projectId, boolean async) {

        String reportRootPath = excelConfig.getSavePath();
        String projectReportRootPath = getSaveFilePath(projectId, reportRootPath, "");

        try {
            LOG.info("删除旧的报表文件...");
            FileUtils.deleteDirectory(new File(projectReportRootPath));
        } catch (IOException e) {
            throw new ExcelReportException("删除旧报表文件失败", e);
        }

        //////////////////////////////////////////////////////////////

        // 初始化缓存
        LOG.info("预加载缓存...");
        reportCacheInitializer.initReportCache(projectId);

        // 初始化任务
        LOG.info("生成任务列表...");
        List<ReportTask> reportTasks = createReportGenerators(projectId);

        // 初始化计数器
        AsyncCounter counter = new AsyncCounter("生成报表", reportTasks.size(), 20);
        counterMap.put(projectId, counter);

        // 初始化线程池
        int poolSize = excelConfig.getPoolSize();
        ThreadPoolExecutor pool = async ? executionPool : createThreadPool(poolSize);

        // 提交任务到线程池
        LOG.info("开始生成报表...");
        for (final ReportTask reportTask : reportTasks) {
            Runnable runnable = () -> {
                try {
                    String filePath = reportTask.getCategory() + "/" + reportTask.getFileName();
                    String saveFilePath = getSaveFilePath(projectId, reportRootPath, filePath);

                    ReportGenerator reportGenerator = applicationContext.getBean(reportTask.getGeneratorClass());
                    reportGenerator.generate(projectId, reportTask.getRange(), reportTask.getTarget(), saveFilePath);

                } catch (Exception e) {
                    LOG.error("生成报表失败: reportTask=" + reportTask, e);
                } finally {
                    counter.count();
                }
            };

            pool.submit(runnable);
        }

        // 满足条件情况下会等待报表生成完毕
        if (isUnitTesting() || !async) {
            try {
                pool.shutdown();
                pool.awaitTermination(1, TimeUnit.DAYS);
                LOG.info("====项目" + projectId + "报表全部生成完毕。");
            } catch (InterruptedException e) {
                LOG.error("====项目" + projectId + "报表生成超时！");
                throw new ExcelReportException(e);
            }
        }
    }

    private ThreadPoolExecutor createThreadPool(int poolSize) {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
    }

    private boolean isUnitTesting() {
        return System.getProperty("unit_testing") != null;
    }

    public List<ReportTask> createReportGenerators(String projectId) {
        return excelConfigParser.parse(projectId, this.reportConfig);
    }

    /**
     * 生成要保存的报表文件路径
     *
     * @param projectId 项目ID
     * @param savePath  报表根目录
     * @param filePath  报表根目录下的文件路径
     *
     * @return 报表文件路径
     */
    public static String getSaveFilePath(String projectId, String savePath, String filePath) {
        String md5 = MD5.digest(projectId);

        return StringUtil.joinPaths(savePath,
                md5.substring(0, 2), md5.substring(2, 4), projectId, filePath);
    }
}
