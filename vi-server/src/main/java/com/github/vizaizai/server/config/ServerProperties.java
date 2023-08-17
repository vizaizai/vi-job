package com.github.vizaizai.server.config;

import cn.hutool.core.collection.CollUtil;
import com.github.vizaizai.remote.utils.Utils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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
    private String mode = "standalone";
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
         * 配置文件路径（优先级更高）
         */
        private String confDir;
        /**
         * 集群节点(127.0.0.1:1141,127.0.0.1:1142,127.0.0.1:1143)
         */
        private List<String> nodes;


        public List<String> getNodes() {
            if (Utils.isNotBlank(confDir)) {
                this.nodes = getAddress(confDir);
                return nodes;
            }
            return nodes;
        }
    }

    private static List<String> getAddress(String path) {
        try {
            List<String> lines = FileUtils.readLines(new File(path), Charset.defaultCharset());
            if (CollUtil.isEmpty(lines)) {
                return Collections.emptyList();
            }
            List<String> addressList = new ArrayList<>();
            for (String line : lines) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                addressList.add(line);
            }
            return addressList;
        }catch (Exception ex) {
            throw new RuntimeException("读取集群地址配置错误，"+ ex.getMessage());
        }
    }
}
