package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * pong
 * @author liaochongwei
 * @date 2023/5/10 16:36
 */
@Data
public class PongDTO {
    /**
     * 本地主机
     */
    private String localhost;
    /**
     * 已绑定的服务主机
     */
    private String bindServerHost;

}
