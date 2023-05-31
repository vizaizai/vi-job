package com.github.vizaizai.remote.server;

import com.github.vizaizai.remote.server.processor.BizProcessor;

/**
 * @author liaochongwei
 * @date 2022/2/18 15:28
 */
public interface Server {

    /**
     * 添加业务处理器
     * @param bizCode 业务码
     * @param bizProcessor 处理器
     */
    void addBizProcessor(String bizCode, BizProcessor bizProcessor);
    /**
     * 启动服务
     */
    void start();
}
