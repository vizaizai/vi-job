package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * 任务运行DTO
 * @author liaochongwei
 * @date 2023/9/7 10:33
 */
@Data
public class JobRunDTO {
    /**
     * 执行器id
     */
    private Long jobId;
    /**
     * 源id
     */
    private String originId;
}
