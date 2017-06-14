package com.xz.scorep.executor.bean;

import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author by fengye on 2017/6/14.
 */
public class TaskProgressTest {

    public static final String PROJECT_ID = "430700-a5a39f1f86b3408d9ced3cf82eb8a1c9";

    public static final String TASK_NAME = ProjectTask.Task.PSS_TASK.name();

    public static final String ACTIVE = ProjectTask.TaskStatus.ACTIVE.name();

    @Test
    public void testIncrFinished() throws Exception {
        TaskProgress taskProgress = new TaskProgress();
        taskProgress.setProjectId(PROJECT_ID);
        taskProgress.setTaskName(TASK_NAME);
        taskProgress.setTaskStatus(ACTIVE);
        taskProgress.setTaskCount(10);

        IntStream.range(0, 10).forEach(i -> new Thread(() -> {
            taskProgress.increaseFinished(taskProgress);
            System.out.println("Thread:" + Thread.currentThread().getName() + ", taskFinished:" + taskProgress.getTaskFinished());
        }).start());

    }
}