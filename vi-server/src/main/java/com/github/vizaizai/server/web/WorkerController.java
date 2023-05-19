package com.github.vizaizai.server.web;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping("/saveOrUpdateWorker")
    public Result<Void> saveOrUpdateWorker(@Validated @RequestBody WorkerUpdateCO workerUpdateCO) {
        return workerService.saveOrUpdateWorker(workerUpdateCO);
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
