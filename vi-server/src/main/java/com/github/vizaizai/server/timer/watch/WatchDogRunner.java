package com.github.vizaizai.server.timer.watch;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.server.constant.Commons;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 执行状态看门狗运行器
 * @author liaochongwei
 * @date 2023/8/9 16:10
 */
@Slf4j
public class WatchDogRunner extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(WatchDogRunner.class);
    /**
     * 监听列表
     */
    private static final Map<String, WatchInstance> watchInstanceMap = new ConcurrentHashMap<>();
    private static volatile WatchDogRunner runner = null;
    private boolean stop = false;

    private WatchDogRunner() {
    }

    public static WatchDogRunner getInstance() {
        if (runner == null) {
            synchronized (WatchDogRunner.class) {
                if (runner == null) {
                    runner = new WatchDogRunner();
                    runner.setName("watch-dog");
                    runner.start();
                    log.info(">>>>>>>>>>Watch dog started");
                }
            }
        }

        return runner;
    }

    /**
     * 判断是否在监听中
     * @param watchId 监听id
     * @return boolean
     */
    public boolean isRunning(String watchId) {
        return watchInstanceMap.containsKey(watchId);
    }

    /**
     * 添加监听实例
     * @param watchInstance 监听实例列表
     */
    public void start(WatchInstance watchInstance) {
        if (watchInstance == null) {
            return;
        }
        if (this.stop) {
            throw new IllegalStateException("Watch runner is stopped");
        }
        watchInstanceMap.putIfAbsent(watchInstance.getWatchId(), watchInstance);
    }

    /**
     * 结束
     * @param watchId 监听id
     */
    public void end(String watchId) {
        WatchInstance removeEle = watchInstanceMap.remove(watchId);
        if (removeEle != null) {
            removeEle.getState().set(State.END);
            if (removeEle.getCompleter() != null) {
                removeEle.getCompleter().exec(removeEle);
            }
        }
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!stop) {
            try {
                if (!watchInstanceMap.isEmpty()) {
                    // 重置时间
                    startTime = System.currentTimeMillis();
                    Set<String> keys = watchInstanceMap.keySet();
                    for (String key : keys) {
                        WatchInstance watchInstance = watchInstanceMap.get(key);
                        if (watchInstance != null
                                && watchInstance.getState().get() == State.WATCH
                                && this.doWatch(watchInstance)) {
                            this.end(watchInstance.getWatchId());
                            break;
                        }
                    }
                }
                // 空闲时间限制停止运行器
                if (System.currentTimeMillis() - startTime > Commons.WATCH_MAX_IDLE
                        && watchInstanceMap.isEmpty()) {
                    runner = null;
                    stop = true;
                    logger.info("Thead[{}] idle {}s", this.getName(), Commons.WATCH_MAX_IDLE / 1000);
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }catch (Exception e) {
                if (!stop) {
                    logger.error("Thead[{}] exception,", this.getName(), e);
                }
            }
        }
    }

    /**
     * 执行监听
     * @param watchInstance 实例
     * @return 是否监听结束
     */
    private boolean doWatch(WatchInstance watchInstance) {
       if (watchInstance.getWatchPredicate() != null) {
           return watchInstance.getWatchPredicate().test(watchInstance);
       }
       return true;
    }


    public static class State {
        /**
         * 监听中
         */
        public static final int WATCH = 1;
        /**
         * 已结束
         */
        public static final int END = 2;

    }
}
