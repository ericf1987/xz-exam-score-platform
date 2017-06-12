package com.xz.scorep.executor.pss.service;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ProjectTask;
import com.xz.scorep.executor.bean.TaskProgress;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author by fengye on 2017/6/12.
 */
public class PssMonitorTest extends BaseTest {

    @Autowired
    PssMonitor pssMonitor;

    public static final String PROJECT_ID = "430700-a5a39f1f86b3408d9ced3cf82eb8a1c9";

    public static final String TASK_NAME = ProjectTask.Task.PSS_TASK.name();

    public static final String ACTIVE = ProjectTask.TaskStatus.ACTIVE.name();

    public static final String TERMINAL = ProjectTask.TaskStatus.TERMINAL.name();

    @Test
    public void testQueryTaskProgress() throws Exception {
        TaskProgress taskProgress = pssMonitor.queryTaskProgress(PROJECT_ID, TASK_NAME);
        System.out.println(taskProgress);
    }

    @Test
    public void testGetProgress() throws Exception {
        double progress = pssMonitor.getProgress(PROJECT_ID, TASK_NAME);
        System.out.println(progress);
    }

    @Test
    public void testAddTaskProgress() throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = format.format(Calendar.getInstance().getTime());

        TaskProgress taskProgress = new TaskProgress();
        taskProgress.setProjectId(PROJECT_ID);
        taskProgress.setTaskName(TASK_NAME);
        taskProgress.setTaskCount(100);
        taskProgress.setTaskFinished(10);
        taskProgress.setStartTime(timeStamp);
        taskProgress.setEndTime(timeStamp);
        taskProgress.setTaskStatus(ACTIVE);

        pssMonitor.addTaskProgress(taskProgress);
    }

    @Test
    public void testIncreaseFinished() throws Exception {
        pssMonitor.increaseFinished(PROJECT_ID, TASK_NAME);
    }
}