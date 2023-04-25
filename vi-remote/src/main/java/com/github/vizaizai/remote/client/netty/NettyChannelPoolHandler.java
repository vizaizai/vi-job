package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcDecoder;
import com.github.vizaizai.remote.codec.RpcEncoder;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.serializer.Serializer;
import com.github.vizaizai.remote.serializer.kryo.KryoSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/4/21 11:06
 */
public class NettyChannelPoolHandler implements ChannelPoolHandler {
    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPoolHandler.class);
    @Override
    public void channelReleased(Channel channel) throws Exception {
        logger.debug("ChannelReleased. Channel ID: " + channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        logger.debug("ChannelAcquired. Channel ID: " + channel.id());
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        SocketChannel ch = (SocketChannel) channel;
        ch.config().setKeepAlive(true);
        ch.config().setTcpNoDelay(true);

        Serializer serializer = new KryoSerializer();
        ChannelPipeline cp = ch.pipeline();
        cp.addLast("beat",new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
        cp.addLast("lengthFieldBasedFrameDecoder",new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast("rpcDecoder",new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast("rpcEncoder",new RpcEncoder(RpcRequest.class, serializer));
        cp.addLast("nettyClientHandler",new NettyClientHandler());
    }
}
