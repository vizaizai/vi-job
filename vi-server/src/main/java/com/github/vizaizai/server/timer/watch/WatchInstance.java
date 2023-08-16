package com.github.vizaizai.server.timer.watch;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 监听实例
 * @author liaochongwei
 * @date 2023/8/9 16:20
 */
@Data
public class WatchInstance {
    /**
     * 监听id
     */
    private String watchId;
    /**
     * 额外参数
     */
    private Map<String, Object> extras;
    /**
     * 监听判断
     */
    private Predicate<WatchInstance> watchPredicate;
    /**
     * 监听结束处理
     */
    private EndProcessor completer;
    /**
     * 状态
     */
    private AtomicInteger state = new AtomicInteger(1);


    public static String getWatchId0(long jobId) {
        return "dp_" + jobId;
    }
    public static String getWatchId1(long jobId) {
        return "exec_" + jobId;
    }
}
