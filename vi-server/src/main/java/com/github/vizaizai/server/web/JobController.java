package com.github.vizaizai.server.web;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.web.co.JobStatusUpdateCO;
import com.github.vizaizai.server.web.co.JobUpdateCO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/addJob")
    public Result<Void> addJob(@RequestBody @Validated JobUpdateCO jobUpdateCO) {
        return jobService.addJob(jobUpdateCO);
    }

    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody @Validated JobStatusUpdateCO jobStatusUpdateCO) {
        return jobService.updateJobStatus(jobStatusUpdateCO);
    }

}
