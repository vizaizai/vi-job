package com.github.vizaizai.server.raft;

import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 状态机
 * @author liaochongwei
 * @date 2023/5/11 19:44
 */
public class RaftStateMachine extends StateMachineAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RaftStateMachine.class);
    private final AtomicLong leaderTerm = new AtomicLong(-1L);

    @Override
    public void onApply(final Iterator it) {
        while (it.hasNext()) {
            LOG.info("===============On apply with term: {} and index: {}. ", it.getTerm(), it.getIndex());
            it.next();
        }
    }

    @Override
    public void onLeaderStart(final long term) {
        super.onLeaderStart(term);
        this.leaderTerm.set(term);
        LOG.info("===========当前成为Leader");
    }

    @Override
    public void onLeaderStop(final Status status) {
        super.onLeaderStop(status);
        this.leaderTerm.set(-1L);
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }
}
