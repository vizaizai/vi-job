package com.github.vizaizai.worker.utils;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.model.JobRunParam;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.netty.NettyServerHandler;
import com.github.vizaizai.worker.core.HttpSender;
import com.github.vizaizai.worker.starter.Commons;
import com.github.vizaizai.worker.starter.PropsKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务Timer
 * @author liaochongwei
 * @date 2023/8/25 14:28
 */
public class JobTimer {
    private static final Logger logger = LoggerFactory.getLogger(JobTimer.class);
    private static final String RUN_URL = "/job/run";
    private static final Map<String, Object> jobMap = new HashMap<>();
    /**
     * 源id
     */
    private static String originId = null;
    /**
     * 执行定时任务
     * @param jobCode 执行器名称
     * @param jobParam 任务参数
     * @param triggerTime 触发时间
     */
    public static void schedule(String jobCode, String jobParam, Long triggerTime) {
        if (originId != null && scheduleForTcp(NettyServerHandler.getSender(originId), jobCode, jobParam, triggerTime)) {
            return;
        }
        boolean schedule = scheduleForHttp(new HttpSender(getRandomServerAddr() + RUN_URL), jobCode, jobParam, triggerTime);
        if (!schedule) {
            throw new RuntimeException("Job schedule fail");
        }
    }
    /**
     * 执行定时任务
     * @param jobCode 执行器名称
     * @param jobParam 任务参数
     * @param triggerTime 触发时间
     */
    @SuppressWarnings("rawtypes")
    private static boolean scheduleForTcp(Sender sender, String jobCode, String jobParam, Long triggerTime) {
        if (sender == null || !sender.available()) {
            return false;
        }
        JobRunParam jobRunParam = new JobRunParam();
        Object jobId = jobMap.get(jobCode);
        if (jobId != null) {
            jobRunParam.setId(Long.parseLong(jobId.toString()));
        }
        jobRunParam.setJobCode(jobCode);
        jobRunParam.setJobParam(jobParam);
        jobRunParam.setTriggerTime(triggerTime);
        long s = System.currentTimeMillis();
        RpcResponse response = (RpcResponse) sender.sendAndRevResponse(RpcRequest.wrap(BizCode.RUN, jobRunParam), 3000);
        logger.info("请求耗时：{}ms",System.currentTimeMillis() - s);
        if (!response.getSuccess()) {
            logger.error("Job schedule fail: {}", response.getMsg());
            return false;
        }
        Result result = (Result) response.getResult();
        if (!result.isSuccess()) {
            logger.error("Job schedule fail: {}", result.getMsg());
            return false;
        }
        return true;
    }
    /**
     * 执行定时任务
     * @param jobCode 执行器名称
     * @param jobParam 任务参数
     * @param triggerTime 触发时间
     */
    @SuppressWarnings("rawtypes")
    private static boolean scheduleForHttp(Sender sender, String jobCode, String jobParam, Long triggerTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", jobMap.get(jobCode));
        params.put("jobCode", jobCode);
        params.put("jobParam", jobParam);
        params.put("triggerTime", DateUtils.format(DateUtils.parse(triggerTime), DateUtils.ST_PATTERN));
        params.put("address", System.getProperty(PropsKeys.BIND_ADDR));
        String response = (String) sender.sendAndRevResponse(params, 3000);
        if (StringUtils.isBlank(response)) {
            logger.error("Job schedule fail: Request http error");
            return false;
        }
        Result result = JSONUtils.parseObject(response, Result.class);
        if (!result.isSuccess()) {
            logger.error("Job schedule fail: {}", result.getMsg());
            return false;
        }
        Map data = (Map) result.getData();
        if (data != null) {
            jobMap.put(jobCode, data.get("jobId"));
            originId = (String) data.get("originId");
        }
        return true;
    }

    /**
     * 获取随机地址
     * @return addr
     */
    private static String getRandomServerAddr() {
        String[] addrs =  Commons.getServerAddr().split(",");
        int nextInt = ThreadLocalRandom.current().nextInt(0, addrs.length);
        return addrs[nextInt];
    }
}
