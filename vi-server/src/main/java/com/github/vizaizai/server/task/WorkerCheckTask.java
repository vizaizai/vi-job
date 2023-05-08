package com.github.vizaizai.server.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.remote.client.Client;
import com.github.vizaizai.remote.client.NettyPoolClient;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.dao.RegistryMapper;
import com.github.vizaizai.server.dao.WorkerMapper;
import com.github.vizaizai.server.entity.Registry;
import com.github.vizaizai.server.entity.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 执行器检查任务
 * @author liaochongwei
 * @date 2023/5/8 17:12
 */
@Slf4j
@Component
public class WorkerCheckTask {


    @Resource
    private WorkerMapper workerMapper;
    @Resource
    private RegistryMapper registryMapper;

    /**
     * 一分钟扫描一次
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void workerConnect() {
        String host = NetUtils.getLocalHost();
        log.info("节点[{}]正在检查worker...", host);
        // 查询当前机器下的所有执行器
        List<Worker> workers = workerMapper.selectList(Wrappers.<Worker>lambdaQuery().eq(Worker::getServerHost, host));
        if (Utils.isEmpty(workers)) {
            return;
        }
        for (Worker worker : workers) {
            List<Registry> registries = registryMapper.selectList(Wrappers.<Registry>lambdaQuery().eq(Registry::getAppName, worker.getAppName()));
            if (Utils.isEmpty(registries)) {
                continue;
            }
            for (Registry registry : registries) {
                String address = registry.getAddress();
                String[] ap = address.split(":");
                Client client = new NettyPoolClient(ap[0], Integer.parseInt(ap[1]));
                Sender sender = client.connect();
                //sender.sendAndRevResponse()
            }
            }

    }
}
