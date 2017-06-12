package com.xz.scorep.executor.pss.service;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.bean.TaskProgress;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author by fengye on 2017/6/12.
 */
@Service
public class PssMonitor {

    @Autowired
    DAOFactory daoFactory;

    public TaskProgress queryTaskProgress(String projectId, String task) {
        DAO managerDao = daoFactory.getManagerDao();
        return managerDao.queryFirst(TaskProgress.class,
                "select * from task_progress where project_id = ? and task_name = ?",
                projectId, task);
    }

    public double getProgress(String projectId, String task) {
        TaskProgress taskProgress = queryTaskProgress(projectId, task);
        return taskProgress.getProgress();
    }

    public void addTaskProgress(TaskProgress taskProgress) {
        DAO managerDao = daoFactory.getManagerDao();
        managerDao.insert(taskProgress, "task_progress");
    }

    public void increaseFinished(String projectId, String task) {
        DAO managerDao = daoFactory.getManagerDao();
        managerDao.execute(
                "update task_progress set task_finished = task_finished + 1 " +
                        "where project_id = ? and task_name = ?",
                projectId, task
        );
    }

}
