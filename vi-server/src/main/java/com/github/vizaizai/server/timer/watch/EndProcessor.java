package com.github.vizaizai.server.timer.watch;

/**
 * 监听结束处理
 * @author liaochongwei
 * @date 2023/8/9 17:03
 */
public interface EndProcessor {
    void exec(WatchInstance watchInstance);
}
