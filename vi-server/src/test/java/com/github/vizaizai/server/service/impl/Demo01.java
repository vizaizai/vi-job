package com.github.vizaizai.server.service.impl;

import com.github.vizaizai.server.raft.SnapshotFile;
import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.metadata.Metadata;

import java.io.IOException;
import java.util.Map;

/**
 * @author liaochongwei
 * @date 2023/7/10 11:21
 */
public class Demo01 {
    public static void main(String[] args) throws IOException {
        SnapshotFile snapshotFile = new SnapshotFile("D:\\tmp\\server1\\snapshot\\snapshot_9\\data");
        KVStorage.initData(snapshotFile.load());
        Map<String, Metadata> mataMap = KVStorage.getMataMap();
        System.out.println(mataMap.size());
    }
}
