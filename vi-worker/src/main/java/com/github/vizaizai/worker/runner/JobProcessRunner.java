package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.contants.ExtendExecStatus;
import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.common.model.TaskTriggerParam;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.log.impl.JobLogger;
import com.github.vizaizai.worker.starter.Commons;
import com.github.vizaizai.worker.utils.DateUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 任务处理运行器（一个jobId对应一个单机运行器）
 * @author liaochongwei
 * @date 2023/4/27 11:26
 */
public class JobProcessRunner extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(JobProcessRunner.class);
    /**
     * 运行器映射(jobId->runner)
     */
    private static final Map<Long, JobProcessRunner> runners = new ConcurrentHashMap<>();
    /**
     * jobId
     */
    private Long jobId;
    /**
     * 任务日志记录器
     */
    private JobLogger jobLogger;
    /**
     * 处理器
     */
    private BasicProcessor processor;
    /**
     * 等待执行的任务
     */
    private LinkedBlockingQueue<TaskContext> waitingTask;
    /**
     * 是否停止
     */
    private boolean stop = false;
    /**
     * 是否在运行中
     */
    private boolean running = false;
    /**
     * 运行时id
     */
    private long runId = 0L;
    /**
     * 获取运行器
     * @param jobId 任务id
     * @param processor 任务处理器
     * @return JobProcessRunner
     */
    public static JobProcessRunner getInstance(Long jobId, BasicProcessor processor) {
        JobProcessRunner runner = runners.get(jobId);
        if (runner != null) {
            return runner;
        }
        runner = new JobProcessRunner();
        runner.jobId = jobId;
        runner.processor = processor;
        runner.waitingTask = new LinkedBlockingQueue<>();
        runner.setName("job-" + jobId);
        runner.start();

        logger.debug(">>>>>>>>>>Thead[{}] started, processor:{}", runner.getName(), processor);
        runners.put(jobId, runner);
        return runner;
    }

    /**
     * 推入待执行队列
     * @param taskContext taskContext
     * @return TaskResult
     */
    public TaskResult pushTaskQueue(TaskContext taskContext) {
        try {
            if (this.stop) {
                return TaskResult.fail("Job runner is stopped");
            }
            Integer maxWaitNum = taskContext.getTriggerParam().getMaxWaitNum();
            if (maxWaitNum != null && waitingTask.size() > maxWaitNum) {
                return TaskResult.fail("Waiting task is too many");
            }
            waitingTask.put(taskContext);
            return TaskResult.ok();
        }catch (Exception e) {
            logger.error("Process-Queue operation error,",e);
            return TaskResult.fail("Process-Queue operation error," + e.getMessage());
        }

    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!stop) {
            this.running = false;
            try {
                TaskContext taskContext = this.waitingTask.poll(5, TimeUnit.SECONDS);
               if (taskContext != null) {
                   this.running = true;
                   this.runId = taskContext.getTriggerParam().getJobInstanceId();
                   TaskTriggerParam triggerParam = taskContext.getTriggerParam();
                   // 构建上报参数
                   StatusReportParam reportParam = new StatusReportParam();
                   reportParam.setJobId(triggerParam.getJobId());
                   reportParam.setJobInstanceId(triggerParam.getJobInstanceId());
                   reportParam.setTriggerType(triggerParam.getTriggerType());
                   reportParam.setExecuteStartTime(System.currentTimeMillis());
                   // 设置日志记录器
                   JobLogger currLogger = new JobLogger(triggerParam.getJobInstanceId(), jobId, DateUtils.parse(triggerParam.getTriggerTime()).toLocalDate());
                   taskContext.setLogger(currLogger);
                   if (jobLogger == null) {
                       this.jobLogger = currLogger;
                   }else {
                       // 日志记录器已过期
                       if (!this.jobLogger.equals(currLogger)) {
                           this.jobLogger.close();
                           this.jobLogger = currLogger;
                       }
                   }
                   jobLogger.info(">>>>>>>>>job#{} start", triggerParam.getJobName());
                   jobLogger.info("param: {}, triggerTime:{}", triggerParam.getJobParams(), triggerParam.getTriggerTime());
                   int execCount = 0;
                   int execResult = 0;
                   int maxExecCount = triggerParam.getRetryCount() != null ? triggerParam.getRetryCount() + 1 : 1;
                   while (execResult != ExecuteStatus.OK.getCode()
                           && execCount < maxExecCount) {
                       if (execCount > 0) {
                           jobLogger.info(">>>>>>>>>job-retry start,count:{}", execCount);
                       }
                       execResult = this.exec(taskContext);
                       execCount++;
                   }
                   jobLogger.info(">>>>>>>>>job finished");
                   this.runId = 0L;
                   reportParam.setExecuteStatus(execResult);
                   reportParam.setExecCount(execCount);
                   reportParam.setExecuteEndTime(System.currentTimeMillis());
                   taskContext.setReportParam(reportParam);
                   // 推入上报队列
                   ReportRunner.getInstance().pushReportQueue(taskContext);
                   // 重置时间
                   startTime = System.currentTimeMillis();
               }else {
                   // 空闲30s停止运行器
                   if (System.currentTimeMillis() - startTime > Commons.MAX_IDLE
                           && waitingTask.size() == 0) {
                       logger.debug("Thead[{}] idle 120s", this.getName());
                       this.shutdown();
                   }
               }
           }catch (Exception e) {
                if (!stop) {
                    logger.error("Thead[{}] exception,", this.getName(), e);
                }
           }
        }
    }


    /**
     * 执行业务
     * @param taskContext 任务上下文
     * @return 执行状态
     */
    private Integer exec(TaskContext taskContext) {
        TaskTriggerParam triggerParam = taskContext.getTriggerParam();
        try {
            Integer timeout = triggerParam.getExecuteTimeout();
            if (timeout != null && timeout > 0) {
                FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
                    // 执行处理器
                    this.processor.execute(taskContext);
                    return true;
                });
                Thread thread = new Thread(futureTask);
                thread.setName("ft-job-" + this.jobId);
                thread.start();
                try {
                    futureTask.get(timeout, TimeUnit.SECONDS);
                    return ExecuteStatus.OK.getCode();
                }catch (TimeoutException te) {
                    // 执行超时
                    jobLogger.warn("job#{} execute timeout", triggerParam.getJobName());
                    return ExecuteStatus.EXEC_TIMEOUT.getCode();
                }
            }else {
                // 执行处理器
                this.processor.execute(taskContext);
                return ExecuteStatus.OK.getCode();
            }
        }catch (Exception ie) {
            jobLogger.error("job#{} execute error,", triggerParam.getJobName(), ie);
            return ExecuteStatus.FAIL.getCode();
        }
    }


    /**
     * 获取执行状态
     * @param runId 运行时id
     * @return 0-未知 1-执行中 2-待执行
     */
    public int status(long runId) {
        if (this.runId == runId) {
            return ExtendExecStatus.ING.getCode();
        }
        boolean match = this.waitingTask
                .stream()
                .anyMatch(e -> e.getTriggerParam().getJobInstanceId().equals(runId));
        if (match) {
            return ExtendExecStatus.WAIT.getCode();
        }
        return ExtendExecStatus.UNKNOWN.getCode();
    }

    /**
     * 取消待执行任务
     * @param runId 运行时id
     * @return boolean
     */
    public boolean cancel(long runId) {
        if (this.runId == runId) {
            throw new RuntimeException("Task is running, cancel fail");
        }
        return this.waitingTask.removeIf(e -> e.getTriggerParam().getJobInstanceId().equals(runId));
    }
    /**
     * 停止
     */
    private void shutdown() {
        try {
            this.waitingTask.clear();
            stop = true;
            running = false;
            jobLogger.close();
            runners.remove(this.jobId);

            if (!this.isInterrupted()) {
                this.interrupt();
            }
        }catch (Exception e) {
            logger.error("Shutdown JobProcessRunner error,", e);
        }
    }

    public static void shutdownAll() {
        runners.forEach((k, v)-> v.shutdown());
    }

    public Long getJobId() {
        return jobId;
    }

    public BasicProcessor getProcessor() {
        return processor;
    }

    public LinkedBlockingQueue<TaskContext> getWaitingTask() {
        return waitingTask;
    }

    public boolean isStop() {
        return stop;
    }

    public boolean isRunning() {
        return running;
    }

    public static JobProcessRunner getRunner(Long jobId) {
        return runners.get(jobId);
    }
}
