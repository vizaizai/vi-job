package com.github.vizaizai.worker.sample.controller;

import com.github.vizaizai.worker.utils.JobTimer;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:02
 */
@RestController
@RequestMapping("/test")
public class UserController {

    @PostMapping("/test1")
    public Object login(@RequestParam Integer s) {

        JobTimer.schedule("job_demoTask1", "renwu参数", System.currentTimeMillis() + 1000L * s);
        return new Result<>(20000,"成功",1);
    }



}
