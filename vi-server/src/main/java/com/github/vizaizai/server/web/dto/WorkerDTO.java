package com.github.vizaizai.server.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行器-DTO
 * @author liaochongwei
 * @date 2023/5/7 17:10
 */
@Data
public class WorkerDTO {

    private Integer id;
    /**
     * 执行器名称
     */
    private String name;
    /**
     * 应用名称
     */
    private String appName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime createTime;
}
