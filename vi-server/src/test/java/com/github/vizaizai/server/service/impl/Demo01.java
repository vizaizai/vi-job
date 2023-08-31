package com.github.vizaizai.server.service.impl;

import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.raft.SnapshotFile;
import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.impl.HashKVStorage;
import com.github.vizaizai.server.raft.kv.metadata.Metadata;

import java.io.IOException;
import java.util.Map;

/**
 * @author liaochongwei
 * @date 2023/7/10 11:21
 */
public class Demo01 {
    public static void main(String[] args) throws IOException, InterruptedException {


        Thread thread2 = new Thread(()->{
            SnapshotFile snapshotFile = new SnapshotFile("D:\\tmp\\server1\\snapshot\\snapshot_76\\data");
            try {
                KVStorage.initData(snapshotFile.load());
                System.out.println("111111111111111111111");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        thread2.start();

        Thread thread1 = new Thread(()->{
            HashKVStorage hashKVStorage = new HashKVStorage();
            hashKVStorage.hPut(Commons.JOB_ASSIGN_KEY, "24","11111111");
            hashKVStorage.hPut(Commons.JOB_ASSIGN_KEY, "25","22222222222");
            System.out.println("22222222222222222222222");
        });
        thread1.start();

        Thread.sleep(1500);
        Map<String, Metadata> mataMap = KVStorage.getMataMap();
        System.out.println(mataMap.size());
    }
}
