package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.remote.codec.RpcDecoder;
import com.github.vizaizai.remote.codec.RpcEncoder;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.serializer.Serializer;
import com.github.vizaizai.remote.serializer.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/4/20 11:36
 */
public class NettyClientInitializer  extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        Serializer serializer = new KryoSerializer();
        ChannelPipeline cp = sc.pipeline();
        cp.addLast("beat",new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
        cp.addLast("lengthFieldBasedFrameDecoder",new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast("rpcDecoder1",new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast("rpcDecoder2",new RpcDecoder(RpcRequest.class, serializer));
        cp.addLast("rpcEncoder1",new RpcEncoder(RpcRequest.class, serializer));
        cp.addLast("rpcEncoder2",new RpcEncoder(RpcResponse.class, serializer));
        cp.addLast("nettyClientHandler",new NettyClientHandler());
    }
}
