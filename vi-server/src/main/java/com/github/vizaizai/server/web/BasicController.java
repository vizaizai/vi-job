package com.github.vizaizai.server.web;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.utils.NetUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础接口
 * @author liaochongwei
 * @date 2023/5/8 15:57
 */
@RestController
public class BasicController {

    @PostMapping("/ping")
    public Result<String> ping() {
        return Result.handleSuccess(NetUtils.getLocalHost());
    }
}
