/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.vizaizai.server.raft;

import com.alipay.sofa.jraft.Lifecycle;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.core.DefaultRaftClientService;
import com.alipay.sofa.jraft.util.internal.ThrowUtil;
import com.github.vizaizai.server.config.ServerProperties;
import com.github.vizaizai.server.raft.processor.assign.JobAssignRequestProcessor;
import com.github.vizaizai.server.raft.processor.assign.JobRmRequestProcessor;
import com.github.vizaizai.server.raft.processor.kv.KVSetRequestProcessor;
import com.github.vizaizai.server.raft.processor.timer.PushIntoTimerRequestProcessor;
import com.github.vizaizai.server.raft.processor.timer.RemoveFromTimerRequestProcessor;
import com.github.vizaizai.server.raft.processor.watch.EndWatchForJobExecRequestProcessor;
import com.github.vizaizai.server.utils.ContextUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jiachun.fjc
 */
@Component
public class RaftServer implements Lifecycle<RaftNodeOptions>, DisposableBean {
    private static final Logger LOG  = LoggerFactory.getLogger(RaftServer.class);
    private RaftGroupService raftGroupService;
    private Node node;
    private RaftStateMachine fsm;
    private boolean started;
    private RaftNodeOptions raftNodeOptions;
    @Resource
    private ServerProperties serverProperties;
    @Value("${server.port}")
    private Integer port;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public boolean init(final RaftNodeOptions opts) {
        if (this.started) {
            LOG.info("[RaftServer: {}] already started.", opts.getServerAddress());
            return true;
        }
        if (!Objects.equals(serverProperties.getMode(), "cluster")) {
            return false;
        }
        opts.initAddress(serverProperties, port);
        this.raftNodeOptions = opts;

        // node options
        NodeOptions nodeOpts = opts.getNodeOptions();
        this.fsm = ContextUtil.getBean(RaftStateMachine.class);
        nodeOpts.setFsm(this.fsm);
        // 初始化配置
        final Configuration initialConf = new Configuration();
        if (!initialConf.parse(opts.getInitialServerAddressList())) {
            throw new IllegalArgumentException("Fail to parse initConf: " + opts.getInitialServerAddressList());
        }
        nodeOpts.setInitialConf(initialConf);
        final String dataPath = opts.getDataPath();
        try {
            FileUtils.forceMkdir(new File(dataPath));
        } catch (final IOException e) {
            LOG.error("Fail to make dir for dataPath {}.", dataPath);
            return false;
        }
        nodeOpts.setLogUri(Paths.get(dataPath, "log").toString());
        nodeOpts.setRaftMetaUri(Paths.get(dataPath, "meta").toString());
        nodeOpts.setSnapshotUri(Paths.get(dataPath, "snapshot").toString());

        final String groupId = opts.getGroupId();
        final PeerId serverId = new PeerId();
        if (!serverId.parse(opts.getServerAddress())) {
            throw new IllegalArgumentException("Fail to parse serverId: " + opts.getServerAddress());
        }

        final RpcServer rpcServer = RaftRpcServerFactory.createRaftRpcServer(serverId.getEndpoint());
        rpcServer.registerProcessor(ContextUtil.getBean(JobAssignRequestProcessor.class));
        rpcServer.registerProcessor(ContextUtil.getBean(JobRmRequestProcessor.class));
        rpcServer.registerProcessor(ContextUtil.getBean(KVSetRequestProcessor.class));
        rpcServer.registerProcessor(new PushIntoTimerRequestProcessor());
        rpcServer.registerProcessor(new RemoveFromTimerRequestProcessor());
        rpcServer.registerProcessor(new EndWatchForJobExecRequestProcessor());

        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOpts, rpcServer);
        this.node = this.raftGroupService.start();
        if (this.node != null) {
            this.started = true;
        }
        countDownLatch.countDown();
        LOG.info("[RaftServer: {}] started.", opts.getServerAddress());
        return this.started;
    }
    @Override
    public void shutdown() {
        if (!this.started) {
            return;
        }
        if (this.raftGroupService != null) {
            this.raftGroupService.shutdown();
            try {
                this.raftGroupService.join();
            } catch (final InterruptedException e) {
                ThrowUtil.throwException(e);
            }
        }
        this.started = false;
        LOG.info("[RaftServer] shutdown successfully: {}.", this);
    }

    public Node getNode() {
        return node;
    }

    public RpcClient getRpcClient() {
        return  ((DefaultRaftClientService)((NodeImpl) this.getNode()).getRpcService()).getRpcClient();
    }

    public RaftStateMachine getFsm() {
        return fsm;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * 等待启动，会阻塞
     */
    public void waitingToStart() {
        try {
            if (isStarted()) {
                return;
            }
            LOG.info("[RaftServer] starting");
            boolean await = countDownLatch.await(10, TimeUnit.SECONDS);
            if (!await) {
                throw new RuntimeException("Wait timeout");
            }
        }catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public boolean isCluster() {
        return Objects.equals(serverProperties.getMode(), "cluster");
    }

    public String getCurrentNodeAddress() {
        if (isCluster() && raftNodeOptions != null) {
            return raftNodeOptions.getServerAddress();
        }
        return null;
    }

    public boolean isLeader() {
        return this.fsm.isLeader();
    }

    /**
     * 获取Leader
     * @return PeerId
     */
    public PeerId getLeader() {
        if (node == null) {
            throw new RuntimeException("JRaft服务未启动");
        }
        PeerId leaderId = node.getLeaderId();
        if (leaderId == null) {
            throw new RuntimeException("集群未就绪");
        }
        return leaderId;
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown();
    }

    public RaftNodeOptions getRaftNodeOptions() {
        return raftNodeOptions;
    }
}
