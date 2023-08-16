package com.github.vizaizai.server.timer.watch;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExtendExecStatus;
import com.github.vizaizai.common.model.ExecStatusQueryParam;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.server.utils.RpcUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 执行情况判断
 * @author liaochongwei
 * @date 2023/8/14 14:37
 */
public class ExecPredicate implements Predicate<WatchInstance> {
    @Override
    public boolean test(WatchInstance watchInstance) {
        Map<String, Object> extras = watchInstance.getExtras();
        ExecStatusQueryParam param = new ExecStatusQueryParam();
        param.setJobId((Long) extras.get("jobId"));
        param.setJobDispatchId((Long) extras.get("dispatchId"));
        // 查询执行状态
        RpcResponse response = RpcUtils.call((String) extras.get("workerAddr"), BizCode.STATUS, param);
        if (response.getSuccess()) {
            Integer status = (Integer) response.getResult();
            return !Objects.equals(status, ExtendExecStatus.ING.getCode()) && !Objects.equals(status, ExtendExecStatus.WAIT.getCode());
        }
        return true;
    }
}
