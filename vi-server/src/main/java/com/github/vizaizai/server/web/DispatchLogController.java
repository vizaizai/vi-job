package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.DispatchLogService;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
import com.github.vizaizai.server.web.dto.DispatchLogDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/6/18411:31
 */
@RestController
@RequestMapping("/dispatch")
public class DispatchLogController {

    @Resource
    private DispatchLogService dispatchLogService;

    @GetMapping("/page")
    public Result<IPage<DispatchLogDTO>> page(@Validated DispatchLogQueryCO queryCO) {
        return dispatchLogService.pageDispatchLogs(queryCO);
    }

}
