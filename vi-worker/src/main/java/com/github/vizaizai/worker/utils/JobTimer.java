package com.github.vizaizai.worker.utils;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.worker.core.HttpSender;
import com.github.vizaizai.worker.starter.Commons;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务Timer
 * @author liaochongwei
 * @date 2023/8/25 14:28
 */
public class JobTimer {

    private static final String RUN_URL = "/job/run";
    /**
     * 执行定时任务
     * @param jobCode 执行器名称
     * @param jobParam 任务参数
     * @param triggerTime 触发时间
     */
    public static void schedule(String jobCode, String jobParam, Long triggerTime) {
        Sender sender = new HttpSender(getRandomServerAddr() + RUN_URL);
        Map<String, Object> params = new HashMap<>();
        params.put("jobCode", jobCode);
        params.put("jobParam", jobParam);
        params.put("triggerTime", DateUtils.format(DateUtils.parse(triggerTime), DateUtils.ST_PATTERN));
        String response = (String) sender.sendAndRevResponse(params, 3000);
        if (StringUtils.isBlank(response)) {
            throw new RuntimeException("Job exec fail: Request http error");
        }
        Result result = JSONUtils.parseObject(response, Result.class);
        if (!result.isSuccess()) {
            throw new RuntimeException("Job exec fail:" + result.getMsg());
        }
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
