package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * id-CO
 * @author liaochongwei
 * @date 2023/5/26 9:43
 */
@Data
public class IdCO {

    @NotNull(message = "数据id必须")
    private String id;
}
