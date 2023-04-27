package com.github.vizaizai.worker.core.processor;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.logging.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Processor运行器（一个jobId对应一个单机运行器）
 * @author liaochongwei
 * @date 2023/4/27 11:26
 */
public class ProcessorRunner extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(ProcessorRunner.class);
    /**
     * 运行器映射(jobId->runner)
     */
    private static final Map<String, ProcessorRunner> runners = new ConcurrentHashMap<>();
    /**
     * jobId
     */
    private String jobId;
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
     * 获取运行器
     * @param jobId 任务id
     * @param processor 任务处理器
     * @return
     */
    public static ProcessorRunner getInstance(String jobId, BasicProcessor processor) {
        ProcessorRunner runner = runners.get(jobId);
        if (runner != null) {
            return runner;
        }
        runner = new ProcessorRunner();
        runner.jobId = jobId;
        runner.processor = processor;
        runner.waitingTask = new LinkedBlockingQueue<>();
        runner.setName("job-" + jobId);
        runner.start();

        logger.info(">>>>>>>>>>Thead[{}] started, processor:{}", runner.getName(), processor);
        runners.put(jobId, runner);
        return runner;
    }

    /**
     * 将任务推入待执行队列
     * @param taskContext taskContext
     */
    public void pushTaskQueue(TaskContext taskContext) {
        try {
            waitingTask.put(taskContext);
        }catch (Exception e) {
            logger.error("Queue operation error,",e);
            throw new RuntimeException("Queue operation error," + e.getMessage());
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
                   long st = System.currentTimeMillis();
                   try {
                       // 执行处理器
                       this.processor.execute(taskContext);
                       // 上报执行结果
                   }catch (Exception ie) {
                       logger.error("{}#{} execute error,",taskContext.getJobName(),taskContext.getJobId(),ie);
                       // 上报执行结果
                   }finally {
                       long eTime = (System.currentTimeMillis() - st) / 1000;
                       // 执行超时
                       if (taskContext.getExecuteTimeout() != null
                               && eTime > taskContext.getExecuteTimeout()) {
                           logger.warn("{}#{} execute timeout.", taskContext.getJobName(),taskContext.getJobId());
                       }
                   }
                   // 重置时间
                   startTime = System.currentTimeMillis();
               }else {
                   // 空闲30s停止运行器
                   if ((System.currentTimeMillis() - startTime) / 1000 >= 30
                           && waitingTask.size() == 0) {
                       stop = true;
                       runners.remove(this.jobId);
                       logger.info("Thead[{}] idle 30s", this.getName());
                       this.interrupt();
                   }
               }
           }catch (Exception e) {
                if (!stop) {
                    logger.error("Thead[{}] exception,", this.getName(), e);
                }
           }
        }
    }

    public String getJobId() {
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
}
