package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.remote.client.event.IdleEventListener;
import com.github.vizaizai.remote.codec.RpcDecoder;
import com.github.vizaizai.remote.codec.RpcEncoder;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.serializer.Serializer;
import com.github.vizaizai.remote.serializer.kryo.KryoSerializer;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/4/20 11:36
 */
public class NettyClientInitializer  extends ChannelInitializer<SocketChannel> {
    private final IdleEventListener idleEventListener;
    /**
     * 业务处理器
     */
    private final Map<String, BizProcessor> bizProcessorMap;

    public NettyClientInitializer(IdleEventListener idleEventListener, Map<String, BizProcessor> bizProcessorMap) {
        this.idleEventListener = idleEventListener;
        this.bizProcessorMap = bizProcessorMap;
    }

    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        Serializer serializer = new KryoSerializer();
        ChannelPipeline cp = sc.pipeline();
        cp.addLast("beat",new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
        cp.addLast("lengthFieldBasedFrameDecoder",new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast("rpcDecoder",new RpcDecoder(RpcMessage.class, serializer));
        cp.addLast("rpcEncoder",new RpcEncoder(RpcMessage.class, serializer));
        cp.addLast("nettyClientHandler",new NettyClientHandler(idleEventListener, bizProcessorMap));
    }
}
