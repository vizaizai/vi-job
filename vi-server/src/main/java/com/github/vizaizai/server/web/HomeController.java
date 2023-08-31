package com.github.vizaizai.server.web;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.HomeService;
import com.github.vizaizai.server.web.dto.CountDTO;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.ServerNodeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:31
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Resource
    private HomeService homeService;

    @GetMapping("/clusters")
    public Result<List<ServerNodeDTO>> clusters() {
        return homeService.clusters();
    }

    @GetMapping("/baseCount")
    public Result<CountDTO> baseCount() {
        return homeService.baseCount();
    }

    @GetMapping("/listWaitingJobs")
    public Result<List<JobDTO>> listWaitingJobs() {
        return homeService.listWaitingJobs();
    }
}
