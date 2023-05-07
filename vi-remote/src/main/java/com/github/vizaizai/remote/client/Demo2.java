package com.github.vizaizai.remote.client;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.remote.common.sender.Sender;
import org.slf4j.Logger;

/**
 *
 * @author liaochongwei
 * @date 2023/4/20 14:45
 */
public class Demo2 {
    private static final Logger logger = LoggerFactory.getLogger(Demo2.class);
    public static void main(String[] args) {

        TaskContext taskContext1 = new TaskContext();
        taskContext1.setJobId("11111");
        taskContext1.setJobName("demoTask1");
        taskContext1.setJobDispatchId("34444444");
        taskContext1.setJobParams("fffffffffef&12");
        taskContext1.setExecuteTimeout(3);

        TaskContext taskContext2 = new TaskContext();
        taskContext2.setJobId("22222");
        taskContext2.setJobName("demoTask2");
        taskContext2.setJobDispatchId("34444444");
        taskContext2.setJobParams("fffffffffef&12");
        taskContext2.setExecuteTimeout(3);

        for (int i = 0; i < 120; i++) {
            long s = System.currentTimeMillis();
            Client client = new NettyPoolClient("192.168.1.101", 3923);
            Sender sender = client.connect();
            logger.info("耗时：{}ms",System.currentTimeMillis() - s);

            RpcRequest request = RpcRequest.wrap(BizCode.RUN, taskContext1);
            Object response = sender.sendAndRevResponse(request.getRequestId(), request, -1);
            //logger.info("收到响应: {}",response);
        }

        for (int i = 0; i < 300; i++) {
            long s = System.currentTimeMillis();
            Client client = new NettyPoolClient("192.168.1.101", 3923);
            Sender sender = client.connect();
            logger.info("耗时：{}ms",System.currentTimeMillis() - s);

            RpcRequest request = RpcRequest.wrap(BizCode.RUN, taskContext2);
            Object response = sender.sendAndRevResponse(request.getRequestId(), request, -1);
            //logger.info("收到响应: {}",response);
        }
    }
}
