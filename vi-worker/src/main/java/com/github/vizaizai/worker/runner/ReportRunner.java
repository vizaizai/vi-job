package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.utils.JSONUtils;
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


    @SuppressWarnings("all")
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!stop) {
            try {
                TaskContext taskContext = this.waitingReports.poll(5, TimeUnit.SECONDS);
                if (taskContext != null) {
                    StatusReportParam reportParam = taskContext.getReportParam();
                    try {
                        Sender sender = taskContext.getSender();
                        if (sender.available()) {
                            RpcResponse response = (RpcResponse) sender.sendAndRevResponse(RpcRequest.wrap(BizCode.REPORT, reportParam), 30000);
                            if (!response.getSuccess()) {
                                return;
                            }
                            Result result = (Result) response.getResult();
                            logger.info("上报结果: {}", JSONUtils.toJSONString(result));

                        }
                    }catch (Exception e) {
                        logger.error("Report error, {}",e.getMessage());
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

    private void doReport(TaskContext taskContext) {

    }
}
