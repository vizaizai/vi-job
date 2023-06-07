package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.web.co.*;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 执行器接口
 * @author liaochongwei
 * @date 2023/5/8 15:57
 */
@RestController
@RequestMapping("/worker")
public class WorkerController {

    @Resource
    private WorkerService workerService;


    @PostMapping("/saveOrUpdate")
    public Result<Void> saveOrUpdateWorker(@Validated @RequestBody WorkerUpdateCO workerUpdateCO) {
        return workerService.saveOrUpdateWorker(workerUpdateCO);
    }

    @GetMapping("/page")
    public Result<IPage<WorkerDTO>> pageWorkers(WorkerQueryCO workerUpdateCO) {
        return workerService.pageWorkers(workerUpdateCO);
    }

    @PostMapping("/remove")
    public Result<Void> removeWorker(@Validated @RequestBody NumberIdCO idCO) {
        return workerService.removeWorker(idCO.getId().intValue());
    }

    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody  RegisterCO registerCO) {
        return workerService.register(registerCO);
    }
    @PostMapping("/unregister")
    public Result<Void> unregister(@Validated @RequestBody  RegisterCO registerCO) {
        return workerService.unregister(registerCO);
    }

}
