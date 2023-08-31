package com.github.vizaizai.server.task;

import com.github.vizaizai.server.dao.dataobject.RegistryDO;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.service.WorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 注册表监视定时任务
 * @author liaochongwei
 * @date 2023/5/8 17:12
 */
@Slf4j
@Component
public class RegistryMonitorTask extends BaseTask{
    @Resource
    private WorkerService workerService;

    /**
     * 一分钟扫描一次
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void check() {
       if (!this.execTask()) {
           return;
       }
        List<RegistryDO> deadRegistries = workerService.listDeadRegistries();
        for (RegistryDO deadRegistry : deadRegistries) {
            workerService.removeRegistry(deadRegistry.getWorkerId(), deadRegistry.getAddress());
        }
    }
}
