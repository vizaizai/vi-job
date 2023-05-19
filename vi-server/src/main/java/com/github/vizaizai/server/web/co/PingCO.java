package com.github.vizaizai.server.web.co;

import lombok.Data;

/**
 * ping-CO
 * @author liaochongwei
 * @date 2023/5/10 16:29
 */
@Data
public class PingCO {

    /**
     * 应用名称
     */
    private String appName;
    /**
     * 调度中心地址
     */
    private String serverAddr;

}
