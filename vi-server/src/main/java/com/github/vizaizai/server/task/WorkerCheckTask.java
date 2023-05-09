package com.github.vizaizai.server.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.remote.client.Client;
import com.github.vizaizai.remote.client.NettyPoolClient;
import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.HeartBeat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final static Map<Integer,Integer> offlineCounts = new HashMap<>();
    /**
     * 一分钟扫描一次
     */
    @Scheduled(fixedDelay = 1000 * 6)
    public void check() {
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
                String[] hostAndPort = address.split(":");
                boolean online = false;
                Client client = null;
                try {
                    client = NettyPoolClient.getInstance(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                    RpcResponse pong = client.request(RpcRequest.wrap(HeartBeat.CODE, "ping"), 5000);
                    // 心跳检查成功且返回
                    if (pong.getSuccess() && Objects.equals(pong.getResult(),"pong")) {
                        online = true;
                    }
                }catch (Exception ignored) {
                }

                if (!online) {
                    // 在线 -> 离线，修改状态
                    if (Objects.equals(1, registry.getOnline())) {
                        log.debug("{} is offline",address);
                        registry.setOnline(0);
                        registryMapper.updateById(registry);
                    }
                    // 离线->离线，记录离线校验次数，5次后删除注册记录，并且移除客户端
                    if (Objects.equals(0, registry.getOnline())) {
                        log.debug("{} is offline",address);
                        Integer count = offlineCounts.getOrDefault(registry.getId(),0);
                        if (count >= 5 && client != null) {
                            client.destroy();
                            registryMapper.deleteById(registry.getId());
                            offlineCounts.remove(registry.getId());
                        }
                        offlineCounts.put(registry.getId(), ++ count);
                    }
                }else {
                    // 离线->在线，修改状态
                    if (Objects.equals(0, registry.getOnline())) {
                        log.debug("{} is online",address);
                        registry.setOnline(1);
                        registryMapper.updateById(registry);
                    }
                }
            }
        }

    }
}
