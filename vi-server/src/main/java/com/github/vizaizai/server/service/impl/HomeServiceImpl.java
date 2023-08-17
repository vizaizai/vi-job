package com.github.vizaizai.server.service.impl;

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.config.ServerProperties;
import com.github.vizaizai.server.dao.JobInstanceMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.RegistryMapper;
import com.github.vizaizai.server.dao.WorkerMapper;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.service.HomeService;
import com.github.vizaizai.server.web.dto.CountDTO;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.ServerNodeDTO;
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
        }

        NodeImpl node = (NodeImpl) raftServer.getNode();

        CliService cliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        Configuration configuration = JRaftUtils.getConfiguration(raftServer.getRaftNodeOptions().getInitialServerAddressList());
        List<PeerId> peers = cliService.getPeers(raftServer.getRaftNodeOptions().getGroupId(), configuration);

        for (PeerId peer : peers) {

        }



        //raftServer.getNode().listPeers()

        return null;
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
    public Result<JobDTO> waitTriggerJobs() {

        return null;
    }
}
