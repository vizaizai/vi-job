package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.dto.CountDTO;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.ServerNodeDTO;

import java.util.List;

/**
 * 首页
 * @author liaochongwei
 * @date 2023/8/16 19:06
 */
public interface HomeService {
    /**
     * 查询集群节点信息
     * @return
     */
    Result<List<ServerNodeDTO>> clusters();

    /**
     * 基础统计
     * @return
     */
    Result<CountDTO> baseCount();

    /**
     * 等待触发的任务列表（最多10条）
     * @return
     */
    Result<List<JobDTO>> listWaitingJobs();
}
