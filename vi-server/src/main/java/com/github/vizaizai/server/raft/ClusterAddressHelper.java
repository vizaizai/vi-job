package com.github.vizaizai.server.raft;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 集群地址Helper
 * @author liaochongwei
 * @date 2023/5/17 17:31
 */
public class ClusterAddressHelper {

    public static List<String> getAddress(String path) {
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
