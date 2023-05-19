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
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import com.alipay.sofa.jraft.util.internal.ThrowUtil;
import com.github.vizaizai.server.raft.processor.JobProtos;
import com.github.vizaizai.server.raft.processor.SyncJobPlanProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author jiachun.fjc
 */
public class RaftServer implements Lifecycle<RaftNodeOptions>, DisposableBean {
    private static final Logger LOG  = LoggerFactory.getLogger(RaftServer.class);
    private RaftGroupService raftGroupService;
    private Node node;
    private RaftStateMachine fsm;
    private boolean started;

    @Override
    public boolean init(final RaftNodeOptions opts) {
        if (this.started) {
            LOG.info("[ElectionNode: {}] already started.", opts.getServerAddress());
            return true;
        }
        // node options
        NodeOptions nodeOpts = opts.getNodeOptions();
        this.fsm = new RaftStateMachine();
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

        final String groupId = opts.getGroupId();
        final PeerId serverId = new PeerId();
        if (!serverId.parse(opts.getServerAddress())) {
            throw new IllegalArgumentException("Fail to parse serverId: " + opts.getServerAddress());
        }
        // 注入序列化类
        RpcFactoryHelper.rpcFactory().registerProtobufSerializer(JobProtos.JobPlanPutRequest.class.getName(),
                JobProtos.JobPlanPutRequest.getDefaultInstance());

        final RpcServer rpcServer = RaftRpcServerFactory.createRaftRpcServer(serverId.getEndpoint());
        rpcServer.registerProcessor(new SyncJobPlanProcessor());

        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOpts, rpcServer);
        this.node = this.raftGroupService.start();
        if (this.node != null) {
            this.started = true;
        }
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
        LOG.info("[ElectionNode] shutdown successfully: {}.", this);
    }

    public Node getNode() {
        return node;
    }

    public RaftStateMachine getFsm() {
        return fsm;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isLeader() {
        return this.fsm.isLeader();
    }

    @Override
    public void destroy() throws Exception {
        this.shutdown();
    }
}
