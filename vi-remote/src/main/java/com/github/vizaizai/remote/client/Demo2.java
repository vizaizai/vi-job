package com.github.vizaizai.remote.client;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.remote.common.sender.Sender;
import org.slf4j.Logger;

/**
 * @author liaochongwei
 * @date 2023/4/20 14:45
 */
public class Demo2 {
    private static final Logger logger = LoggerFactory.getLogger(Demo2.class);
    public static void main(String[] args) {
        Client client = new NettyPoolClient("127.0.0.1", 7070);
        Sender sender = client.connect();
        TaskContext taskContext = new TaskContext();
        taskContext.setJobId("11111");
        taskContext.setJobName("testJob");
        taskContext.setJobDispatchId("34444444");
        taskContext.setJobParams("fffffffffef&12");

        logger.info("开始发送");
        RpcRequest request = RpcRequest.wrap(BizCode.RUN, taskContext);
        Object response = sender.sendAndRevResponse(request.getRequestId(), request, -1);
        logger.info("收到响应: {}",response);
    }
}
