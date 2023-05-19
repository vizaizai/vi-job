package com.github.vizaizai.server.service.impl;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.dao.DispatchLogMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.JobAddCO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/5/18 19:37
 */
@Service
@Slf4j
public class JobServiceImpl implements JobService {
    @Resource
    private JobMapper jobMapper;
    @Resource
    private DispatchLogMapper dispatchLogMapper;


    @Override
    public Result<Void> addJob(JobAddCO jobAddCO) {
        jobAddCO.setCreater(UserUtils.getUserName());
        JobDO job = BeanUtils.toBean(jobAddCO, JobDO::new);
        jobMapper.insert(job);



        return Result.ok("新增成功");
    }
}
