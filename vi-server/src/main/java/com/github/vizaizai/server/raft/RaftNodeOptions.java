
package com.github.vizaizai.server.raft;

import cn.hutool.core.collection.CollUtil;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.server.config.ServerProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jiachun.fjc
 */
public class RaftNodeOptions {
    private String dataPath;
    // raft group id
    private String groupId;
    // ip:port
    private String serverAddress;
    // ip:port,ip:port,ip:port
    private String initialServerAddressList;
    // raft node options
    private final NodeOptions nodeOptions;

    public RaftNodeOptions() {
        this.nodeOptions = new NodeOptions();
        this.nodeOptions.setElectionTimeoutMs(30000);
        this.nodeOptions.setSnapshotIntervalSecs(600);
        this.setGroupId("vi-server");
    }

    public void initAddress(ServerProperties serverProperties, int port) {
        // 数据文件夹
        this.setDataPath(serverProperties.getDataDir());
        // 节点地址
        this.setServerAddress(serverProperties.getInetutils().getIpAddress() + ":" + (port + 1000));
        // 初始化集群地址
        List<String> nodes = serverProperties.getCluster().getNodes().stream().map(e -> {
            Pair<String, Integer> pair = NetUtils.splitAddress2IpAndPort(e);
            return pair.getKey() + ":" + (pair.getValue() + 1000);
        }).collect(Collectors.toList());
        // 当前节点地址必须在集群列表里
        if (!nodes.contains(this.serverAddress)) {
            throw new RuntimeException("当前节点地址必须在集群列表里");
        }
        this.setInitialServerAddressList(CollUtil.join(nodes, ","));
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getInitialServerAddressList() {
        return initialServerAddressList;
    }

    public void setInitialServerAddressList(String initialServerAddressList) {
        this.initialServerAddressList = initialServerAddressList;
    }

    public NodeOptions getNodeOptions() {

        return nodeOptions;
    }


    @Override
    public String toString() {
        return "ElectionNodeOptions{" + "dataPath='" + dataPath + '\'' + ", groupId='" + groupId + '\''
               + ", serverAddress='" + serverAddress + '\'' + ", initialServerAddressList='" + initialServerAddressList
               + '\'' + ", nodeOptions=" + nodeOptions + '}';
    }
}
