package com.github.vizaizai.server.web;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.DispatchLogService;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.co.NumberIdCO;
import com.github.vizaizai.server.web.dto.DispatchLogDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    @GetMapping("/getLog")
    public Result<LogInfo> getLog(@Validated LogQueryCO queryCO) {
        return dispatchLogService.getLog(queryCO);
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(@Validated @RequestBody NumberIdCO idCO) {
        return dispatchLogService.cancel(idCO.getId());
    }

    @PostMapping("/remove")
    public Result<Void> remove(@Validated @RequestBody NumberIdCO idCO) {
        return dispatchLogService.remove(idCO.getId());
    }
}
