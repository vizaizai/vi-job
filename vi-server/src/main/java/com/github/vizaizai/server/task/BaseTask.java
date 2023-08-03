package com.github.vizaizai.server.task;

import com.github.vizaizai.server.raft.RaftServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/8/2 11:25
 */
@Component
public class BaseTask {

    @Resource
    protected RaftServer raftServer;
    /**
     * 是否首次执行
     */
    private static boolean firstExecute = true;
    /**
     * 判断是否允许执行
     * @return true-允许 false-不允许
     */
    public boolean execTask() {
        if (raftServer.isCluster()) {
            // 集群环境首次执行延时5秒，避免快照数据未加载导致获取不到数据
            if (firstExecute) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception ignored) {
                }
                firstExecute = false;
            }
            raftServer.waitingToStart();
            return raftServer.isLeader();
        }
        return true;
    }


}
