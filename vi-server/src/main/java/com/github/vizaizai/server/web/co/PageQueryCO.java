package com.github.vizaizai.server.web.co;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 分页查询-CO
 * @author liaochongwei
 * @date 2023/5/26 15:46
 */
@Data
public class PageQueryCO {
    /**
     * 页码
     */
    @NotNull(message = "页码必须")
    private Integer page;
    /**
     * 页长
     */
    @NotNull(message = "页长必须")
    private Integer limit;


    public <T> Page<T> toPage() {
        return Page.of(page, limit);
    }

}
