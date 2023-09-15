package com.github.vizaizai.worker.sample.controller;

import com.github.vizaizai.worker.utils.JobTimer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:02
 */
@RestController
@RequestMapping("/test")
public class UserController {

    @PostMapping("/test1")
    public Object login(@RequestParam Integer s) {
        for (int i = 0; i < 20; i++) {
            long st = System.currentTimeMillis();
            JobTimer.schedule("job_demoTask1", "renwu参数", System.currentTimeMillis() + 1000L * s);
            System.out.println("调度耗时：" + (System.currentTimeMillis() - st) + "ms");
        }
        return new Result<>(20000,"成功",1);
    }
}
