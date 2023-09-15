package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.HttpSender;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.starter.Commons;
import com.github.vizaizai.worker.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * 调度中心地址
     */
    private String serverAddr;
    /**
     * 待上报队列
     */
    private static final BlockingQueue<TaskContext> waitingReports = new LinkedBlockingQueue<>();
    private static volatile ReportRunner runner = null;
    private boolean stop = false;

    private ReportRunner() {
    }

    public static ReportRunner getInstance() {
        if (runner == null) {
            synchronized (ReportRunner.class) {
                if (runner == null) {
                    runner = new ReportRunner();
                    runner.serverAddr = Commons.getServerAddr();
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
            if (this.stop) {
                 throw new IllegalStateException("Report runner is stopped");
            }
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
                List<TaskContext> taskContexts = new ArrayList<>();
                int count = waitingReports.drainTo(taskContexts, 30);
                if (count > 0) {
                    List<StatusReportParam> params = taskContexts.stream().map(TaskContext::getReportParam).collect(Collectors.toList());Sender sender = null;
                    if (!taskContexts.isEmpty()) {
                        sender = taskContexts.get(0).getSender();
                    }
                    try {
                        this.doReport(sender, params);
                    }catch (Exception e) {
                        logger.warn("Report batch fail, {}", e.getMessage());
                        // 上报失败，保存重试参数
                        ReportRetryRunner.getInstance().writeRetryParam(params);
                    }
                    // 重置时间
                    startTime = System.currentTimeMillis();
                }else {
                    // 空闲120s停止运行器
                    if (System.currentTimeMillis() - startTime > Commons.MAX_IDLE
                            && waitingReports.size() == 0) {
                        runner = null;
                        stop = true;
                        logger.debug("Thead[{}] idle 120s", this.getName());
                    }
                }
                TimeUnit.SECONDS.sleep(5);
            }catch (Exception e) {
                if (!stop) {
                    logger.error("Thead[{}] exception,", this.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void doReport(Sender sender, List<StatusReportParam> params) {
        if (Utils.isEmpty(params)) {
            return;
        }
        if (sender != null && sender.available()) {
            RpcResponse response = (RpcResponse) sender.sendAndRevResponse(RpcRequest.wrap(BizCode.REPORT, params), 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException(response.getMsg());
            }
            Result result = (Result) response.getResult();
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMsg());
            }
        }else {
            // http上报
            sender = new HttpSender(this.getRandomServerAddr() + STATUS_REPORT_URL);
            String response = (String) sender.sendAndRevResponse(params, 3000);
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("http请求异常");
            }
            Result result = JSONUtils.parseObject(response, Result.class);
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMsg());
            }
        }
    }

    /**
     * 获取随机地址
     * @return addr
     */
    private String getRandomServerAddr() {
        String[] addrs = serverAddr.split(",");
        int nextInt = ThreadLocalRandom.current().nextInt(0, addrs.length);
        return addrs[nextInt];
    }
}
