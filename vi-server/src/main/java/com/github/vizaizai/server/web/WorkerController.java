package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.web.co.NumberIdCO;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerQueryCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;
import com.github.vizaizai.server.web.dto.RegistryDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private GlobalJobGroupManager globalJobTriggerTimer;

    @GetMapping("/foo")
    public Result<Void> foo(@RequestParam Long jobId) {
        globalJobTriggerTimer.elect(jobId);
        return Result.ok();
    }

    @GetMapping("/bar")
    public Result<Void> bar(@RequestParam Long jobId) {
        globalJobTriggerTimer.remove(jobId);
        return Result.ok();
    }

    @PostMapping("/saveOrUpdate")
    public Result<Void> saveOrUpdateWorker(@Validated @RequestBody WorkerUpdateCO workerUpdateCO) {
        return workerService.saveOrUpdateWorker(workerUpdateCO);
    }

    @GetMapping("/page")
    public Result<IPage<WorkerDTO>> pageWorkers(WorkerQueryCO workerQueryCO) {
        return workerService.pageWorkers(workerQueryCO);
    }
    @GetMapping("/nodes")
    public Result<List<RegistryDTO>> listWorkerNodes(@RequestParam Integer workerId) {
        return workerService.listWorkerNodes(workerId);
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
