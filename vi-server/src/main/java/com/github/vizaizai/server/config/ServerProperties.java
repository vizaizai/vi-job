package com.github.vizaizai.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author liaochongwei
 * @date 2023/5/16 16:36
 */
@ConfigurationProperties(prefix = "vi-job")
@Data
public class ServerProperties {
    /**
     * 部署模式 standalone,cluster
     */
    private String mode;
    /**
     * 集群配置
     */
    private Cluster cluster;
    /**
     * 网络配置
     */
    private InetUtils inetutils;
    /**
     *  Raft数据路径， 默认{baseDir}/raft/
     */
    private String dataDir;

    @Data
    public static class InetUtils{
        /**
         * 绑定ip地址
         */
        private String ipAddress = "127.0.0.1";
    }


    @Data
    public static class Cluster {
        /**
         * 配置文件路径
         */
        private String confDir;
        /**
         * 集群节点(127.0.0.1:2141,127.0.0.1:2142,127.0.0.1:2143)
         */
        private List<String> nodes;
    }
}
