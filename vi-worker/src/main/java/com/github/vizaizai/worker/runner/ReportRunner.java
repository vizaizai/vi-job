package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.worker.core.HttpSender;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 任务上报运行器
 * @author liaochongwei
 * @date 2023/5/31 17:19
 */
public class ReportRunner extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(ReportRunner.class);
    /**
     * 状态上报
     */
    private static final String STATUS_REPORT_URL = "/job/statusReport";
    /**
     * 待上报队列
     */
    private final BlockingQueue<TaskContext> waitingReports = new LinkedBlockingQueue<>();
    private static volatile ReportRunner runner = null;
    private boolean stop = false;

    private ReportRunner() {
    }

    public static ReportRunner getInstance() {
        if (runner == null) {
            synchronized (ReportRunner.class) {
                if (runner == null) {
                    runner = new ReportRunner();
                    runner.setName("reporter");
                    runner.start();
                }
            }
        }
        return runner;
    }

    /**
     * 推入上报队列
     * @param taskContext 任务上下文
     */
    public void pushReportQueue(TaskContext taskContext) {
        try {
            waitingReports.put(taskContext);
        }catch (Exception e) {
            logger.error("Report-Queue operation error,",e);
            throw new RuntimeException("Report-Queue operation error," + e.getMessage());
        }
    }


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!stop) {
            try {
                TaskContext taskContext = this.waitingReports.poll(5, TimeUnit.SECONDS);
                if (taskContext != null) {
                    try {
                        this.doReport(taskContext);
                    }catch (Exception e) {
                        logger.warn("job#{} report fail, {}",taskContext.getTriggerParam().getJobName(), e.getMessage());
                        this.waitingReports.put(taskContext);
                    }
                    // 重置时间
                    startTime = System.currentTimeMillis();
                }else {
                    // 空闲120s停止运行器
                    if ((System.currentTimeMillis() - startTime) / 1000 >= 120
                            && waitingReports.size() == 0) {
                        runner = null;
                        stop = true;
                        logger.info("Thead[{}] idle 120s", this.getName());
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

    @SuppressWarnings("rawtypes")
    private void doReport(TaskContext taskContext) {
        Sender sender = taskContext.getSender();
        if (sender != null && sender.available()) {
            RpcResponse response = (RpcResponse) sender.sendAndRevResponse(RpcRequest.wrap(BizCode.REPORT, taskContext.getReportParam()), 30000);
            if (!response.getSuccess()) {
                throw new RuntimeException(response.getMsg());
            }
            Result result = (Result) response.getResult();
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMsg());
            }

        }else {
            // http上报
            sender = new HttpSender(STATUS_REPORT_URL);
            String response = (String) sender.sendAndRevResponse(taskContext.getReportParam(), 30000);
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("http请求异常");
            }
            Result result = JSONUtils.parseObject(response, Result.class);
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMsg());
            }
        }
    }
}
