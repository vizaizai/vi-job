package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * ping
 * @author liaochongwei
 * @date 2023/5/10 16:36
 */
@Data
public class PingResult {
    /**
     * 源id
     */
    private String originId;
    /**
     * 是否成功
     */
    private Boolean success;

}
