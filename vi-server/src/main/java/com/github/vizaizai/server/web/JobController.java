package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.web.co.*;
import com.github.vizaizai.server.web.dto.JobDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/5/18 11:31
 */
@RestController
@RequestMapping("/job")
public class JobController {

    @Resource
    private JobService jobService;

    @GetMapping("/page")
    public Result<IPage<JobDTO>> page(@Validated JobQueryCO jobQueryCO) {
        return jobService.pageJobs(jobQueryCO);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Validated JobUpdateCO jobUpdateCO) {
        return jobService.addJob(jobUpdateCO);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Validated JobUpdateCO jobUpdateCO) {
        return jobService.updateJob(jobUpdateCO);
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody @Validated IdCO idCO) {
        return jobService.removeJob(Long.valueOf(idCO.getId()));
    }

    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody @Validated JobStatusUpdateCO jobStatusUpdateCO) {
        return jobService.updateJobStatus(jobStatusUpdateCO);
    }

    @PostMapping("/run")
    public Result<Void> run(@RequestBody @Validated JobRunCO jobRunCO) {
        return jobService.run(jobRunCO);
    }

    @PostMapping("/statusReport")
    public Result<Void> statusReport(@RequestBody @Validated StatusReportCO statusReportCO) {
        return jobService.statusReport(statusReportCO);
    }

}
