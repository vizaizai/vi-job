package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.JobInstanceService;
import com.github.vizaizai.server.web.co.JobInstanceQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.co.NumberIdCO;
import com.github.vizaizai.server.web.dto.JobInstanceDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/6/18411:31
 */
@RestController
@RequestMapping("/jobInstance")
public class JobInstanceController {

    @Resource
    private JobInstanceService jobInstanceService;

    @GetMapping("/page")
    public Result<IPage<JobInstanceDTO>> page(@Validated JobInstanceQueryCO queryCO) {
        return jobInstanceService.pageJobInstances(queryCO);
    }

    @GetMapping("/getLog")
    public Result<LogInfo> getLog(@Validated LogQueryCO queryCO) {
        return jobInstanceService.getLog(queryCO);
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(@Validated @RequestBody NumberIdCO idCO) {
        return jobInstanceService.cancel(idCO.getId());
    }

    @PostMapping("/remove")
    public Result<Void> remove(@Validated @RequestBody NumberIdCO idCO) {
        return jobInstanceService.remove(idCO.getId());
    }
}
