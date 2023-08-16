package com.github.vizaizai.server.timer.watch;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Predicate;

/**
 * 调度情况判断
 * @author liaochongwei
 * @date 2023/8/14 14:37
 */
public class DispatchPredicate implements Predicate<WatchInstance> {
    @SuppressWarnings("unchecked")
    @Override
    public boolean test(WatchInstance watchInstance) {
        Map<String, Object> extras = watchInstance.getExtras();
        Future<Boolean> future  = (Future<Boolean>) extras.get("future");
        if (future != null) {
            return future.isDone();
        }
        return true;
    }
}
