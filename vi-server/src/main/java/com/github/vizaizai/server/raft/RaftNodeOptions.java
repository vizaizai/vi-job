
package com.github.vizaizai.server.raft;

import com.alipay.sofa.jraft.option.NodeOptions;

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
        this.nodeOptions.setElectionTimeoutMs(3000);
        this.nodeOptions.setLeaderLeaseTimeRatio(3);
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
