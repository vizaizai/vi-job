package com.github.vizaizai.remote.client;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.BizCode;
import org.slf4j.Logger;

/**
 *
 * @author liaochongwei
 * @date 2023/4/20 14:45
 */
public class Demo2 {
    private static final Logger logger = LoggerFactory.getLogger(Demo2.class);
    public static void main(String[] args) throws InterruptedException {

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

        Client client = NettyPoolClient.getInstance("192.168.233.1", 3923);
        for (int i = 0; i < 3; i++) {
            try {
                long s = System.currentTimeMillis();
                RpcRequest request = RpcRequest.wrap(BizCode.STOP, taskContext1);
                RpcResponse response = client.request(request, 1500);
                logger.info("收到响应: {}，耗时:{}ms",response,(System.currentTimeMillis()- s));
            }catch (Exception e) {
                logger.error("错误：",e);
            }
        }

        //NettyConnectionPool.getInstance().remove(new InetSocketAddress("192.168.233.1",3923));
    }
}
