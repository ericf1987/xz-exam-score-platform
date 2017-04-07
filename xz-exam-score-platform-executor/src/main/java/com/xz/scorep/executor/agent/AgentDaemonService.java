package com.xz.scorep.executor.agent;

import com.alibaba.fastjson.JSON;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.config.ManagerConfig;
import com.xz.scorep.executor.db.DAOFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AgentDaemonService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentDaemonService.class);

    private static final String DBSIZE_SQL =
            "select SUM(DATA_LENGTH + INDEX_LENGTH) as DATA_SIZE from information_schema.TABLES";

    private static final String PROJECT_SQL =
            "select id, status from project";

    private Thread daemonThread;

    private String heartbeatUrl, projectListUrl;

    private boolean managerOnline;

    @Autowired
    private ManagerConfig managerConfig;

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

    @PostConstruct
    private void initAgentDaemonService() {
        initThread();
    }

    protected long getDatabaseSize() {
        return cacheFactory.getGlobalCache().get("__db_size", () -> {
            Row row = daoFactory.getRootDao().queryFirst(DBSIZE_SQL);
            return row.getLong("DATA_SIZE", 0);
        }, 60);
    }

    protected List<Map<String, Object>> getProjectStatus() {
        return cacheFactory.getGlobalCache().get("__project_status", () -> {
            List<Map<String, Object>> statusList = daoFactory.getManagerDao()
                    .query(PROJECT_SQL)
                    .stream().map(this::row2Status)
                    .collect(Collectors.toList());
            return new ArrayList<>(statusList);
        }, 10);
    }

    protected List<String> getProjectIdList() {
        return daoFactory.getManagerDao().query("select id from project")
                .stream().map(row -> row.getString("id")).collect(Collectors.toList());
    }

    private Map<String, Object> row2Status(Row row) {
        return new MapBuilder<String, Object>()
                .and("projectId", row.getString("id"))
                .and("status", row.getString("status")).get();
    }

    private void initThread() {
        if (StringUtils.isBlank(managerConfig.getHost())) {
            return;
        }

        if (managerConfig.getPort() == 0) {
            return;
        }

        this.heartbeatUrl = "http://" + managerConfig.getHost() + ":" + managerConfig.getPort() + "/agent/heartbeat";
        this.projectListUrl = "http://" + managerConfig.getHost() + ":" + managerConfig.getPort() + "/agent/projects";

        this.daemonThread = new Thread(this::run);
        this.daemonThread.setDaemon(true);
        this.daemonThread.start();
    }

    private void run() {
        while (true) {
            try {
                run0();
                Thread.sleep(Math.max(
                        managerConfig.getInterval(), ManagerConfig.MIN_INTERVAL));
            } catch (Exception e) {
                LOG.error("后台保持进程错误", e);
            }
        }
    }

    private void run0() throws IOException {

        boolean managerOnlineBefore = managerOnline;
        sendHeartbeat();

        // 如果管理服务器从之前的下线状态恢复，则主动推送一波项目列表
        if (!managerOnlineBefore && managerOnline) {
            pushProjectList();
        }
    }

    private void pushProjectList() throws IOException {
        try {
            HttpRequest httpRequest = new HttpRequest(projectListUrl)
                    .setParameter("host", System.getProperty("server.address"))
                    .setParameter("port", System.getProperty("server.port"))
                    .setParameter("projects", JSON.toJSONString(getProjectIdList()));

            String resultJson = httpRequest.requestPost();
            Result result = JSON.parseObject(resultJson, Result.class);

            if (!result.isSuccess()) {
                LOG.error("心跳请求出错: " + result.getMessage());
            }
        } catch (IOException e) {
            managerOnline = false;
            throw e;
        }
    }

    private void sendHeartbeat() throws IOException {
        HttpRequest httpRequest = new HttpRequest(heartbeatUrl)
                .setParameter("host", System.getProperty("server.address"))
                .setParameter("port", System.getProperty("server.port"))
                .setParameter("dataSize", getDatabaseSize())
                .setParameter("status", JSON.toJSONString(getProjectStatus()));

        String resultJson = httpRequest.requestPost();
        Result result = JSON.parseObject(resultJson, Result.class);

        if (!result.isSuccess()) {
            LOG.error("心跳请求出错: " + result.getMessage());
        }
    }

    public boolean isAlive() {
        return this.daemonThread != null && this.daemonThread.isAlive();
    }
}
