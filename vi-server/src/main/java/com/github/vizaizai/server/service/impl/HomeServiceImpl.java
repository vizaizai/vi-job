package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.config.ServerProperties;
import com.github.vizaizai.server.constant.JobStatus;
import com.github.vizaizai.server.dao.JobInstanceMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.RegistryMapper;
import com.github.vizaizai.server.dao.WorkerMapper;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.service.HomeService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.web.co.JobQueryCO;
import com.github.vizaizai.server.web.dto.CountDTO;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.ServerNodeDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liaochongwei
 * @date 2023/8/16 19:09
 */
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private JobMapper jobMapper;
    @Resource
    private JobInstanceMapper jobInstanceMapper;
    @Resource
    private WorkerMapper workerMapper;
    @Resource
    private RegistryMapper registryMapper;
    @Resource
    private RaftServer raftServer;
    @Resource
    private ServerProperties serverProperties;

    @Override
    public Result<List<ServerNodeDTO>> clusters() {
        List<ServerNodeDTO> serverNodes = new ArrayList<>();
        if (!raftServer.isCluster()) {
            return Result.handleSuccess(serverNodes);
        }
        List<String> nodes = serverProperties.getCluster().getNodes();
        for (String node : nodes) {
            ServerNodeDTO serverNode = new ServerNodeDTO();
            serverNode.setAddress(node);
            serverNode.setState(false);
            serverNode.setLeader(false);
            serverNodes.add(serverNode);
        }

        PeerId leader = raftServer.getLeader();

        CliService cliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        Configuration configuration = JRaftUtils.getConfiguration(raftServer.getRaftNodeOptions().getInitialServerAddressList());
        List<PeerId> alivePeers = cliService.getAlivePeers(raftServer.getRaftNodeOptions().getGroupId(), configuration);

        for (ServerNodeDTO serverNode : serverNodes) {
            Pair<String, Integer> pair = NetUtils.splitAddress2IpAndPort(serverNode.getAddress());
            String ip = pair.getKey();
            int port = pair.getValue() + 1000;
            if (leader != null
                    && ip.equals(leader.getIp())
                    && port == leader.getPort()) {
                serverNode.setLeader(true);
            }
            serverNode.setState(alivePeers.stream().anyMatch(e->ip.equals(e.getIp()) && port == e.getPort()));
        }

        return Result.handleSuccess(serverNodes);
    }

    @Override
    public Result<CountDTO> baseCount() {
        CountDTO countDTO = new CountDTO();
        countDTO.setTotalJobNum(jobMapper.selectCount(null));
        countDTO.setRunningInstanceNum(jobInstanceMapper.selectCount(Wrappers.<JobInstanceDO>lambdaQuery()
                .eq(JobInstanceDO::getExecuteStatus, ExecuteStatus.ING.getCode())));
        countDTO.setWorkerNum(workerMapper.selectCount(null));
        countDTO.setWorkerNodeNum(registryMapper.selectCount(null));
        return Result.handleSuccess(countDTO);
    }

    @Override
    public Result<List<JobDTO>> listWaitingJobs() {
        JobQueryCO jobQueryCO = new JobQueryCO();
        jobQueryCO.setPage(1);
        jobQueryCO.setLimit(10);
        LambdaQueryWrapper<JobDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(JobDO::getStatus, JobStatus.RUN.getCode());
        queryWrapper.orderByAsc(JobDO::getNextTriggerTime);
        Page<JobDO> jobDOPage = jobMapper.selectPage(jobQueryCO.toPage(), queryWrapper);
        List<JobDTO> jobs = BeanUtils.toBeans(jobDOPage.getRecords(), JobDTO::new);
        if (Utils.isNotEmpty(jobs)) {
            for (JobDTO job : jobs) {
                if (job.getNextTriggerTime() != null) {
                    job.setNextTriggerTime0(LocalDateTimeUtil.of(job.getNextTriggerTime()));
                }

            }
        }
        return Result.handleSuccess(jobs);
    }
}
