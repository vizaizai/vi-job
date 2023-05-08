package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 执行器新增编辑-CO
 * @author liaochongwei
 * @date 2023/5/8 14:19
 */
@Data
public class WorkerUpdateCO {
    /**
     * id
     */
    private Integer id;
    /**
     * 执行器名称
     */
    private String name;
    /**
     * 应用
     */
    private String appName;

}
