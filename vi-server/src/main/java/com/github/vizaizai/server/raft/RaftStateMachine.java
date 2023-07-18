package com.github.vizaizai.server.raft;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.raft.kv.KVCommand;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.KVStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 状态机
 * @author liaochongwei
 * @date 2023/5/11 19:44
 */
@Slf4j
@Component
public class RaftStateMachine extends StateMachineAdapter {
    private final AtomicLong leaderTerm = new AtomicLong(-1L);

    @Override
    public void onApply(final Iterator iter) {
        while (iter.hasNext()) {
            log.info("onApply：logIndex={}", iter.getIndex());
            Result<Object> result = Result.ok();
            KVCommand command = null;
            KVOpClosure closure = null;
            // 如果是leader，done不为空
            if (iter.done() != null) {
                closure = (KVOpClosure) iter.done();
                command = closure.getCommand();
            } else {
                final ByteBuffer data = iter.getData();
                try {
                    command = SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(
                            data.array(), KVCommand.class.getName());
                } catch (final CodecException e) {
                    log.error("Fail to decode KVCommand", e);
                    result = Result.handleFailure(e.getMessage());
                }
            }

            try {
                if (command != null) {
                    result.setData(KVStorage.execute(command));
                }
            }catch (Exception e) {
                result = Result.handleFailure(e.getMessage());
            }

            if (closure != null) {
                closure.setResult(result);
                closure.run(result.isSuccess() ? Status.OK() : new Status(RaftError.EINTERNAL, result.getMsg()));
            }
            iter.next();
        }
    }

    @Override
    public void onLeaderStart(final long term) {
        super.onLeaderStart(term);
        this.leaderTerm.set(term);
        log.info("===========LeaderStart=========");
    }

    @Override
    public void onLeaderStop(final Status status) {
        super.onLeaderStop(status);
        this.leaderTerm.set(-1L);
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        SnapshotFile snapshot = new SnapshotFile(writer.getPath() + File.separator + Commons.SNAPSHOT_NAME);
        byte[] data = KVStorage.loadData();
        if (snapshot.save(data)) {
            if (writer.addFile(Commons.SNAPSHOT_NAME)) {
                done.run(Status.OK());
            } else {
                done.run(new Status(RaftError.EIO, "Fail to add file to writer"));
            }
        } else {
            done.run(new Status(RaftError.EIO, "Fail to save snapshot %s", snapshot.getPath()));
        }
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (isLeader()) {
            log.warn("Leader is not supposed to load snapshot");
            return false;
        }
        if (reader.getFileMeta("data") == null) {
            log.error("Fail to find data file in {}", reader.getPath());
            return false;
        }
        SnapshotFile snapshot = new SnapshotFile(reader.getPath() + File.separator + "data");
        try {
            KVStorage.initData(snapshot.load());
            return true;
        } catch (final IOException e) {
            log.error("Fail to load snapshot from {}", snapshot.getPath());
            return false;
        }

    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }
}
