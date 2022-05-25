package binary.wz.im.connector.remoting;

import binary.wz.im.common.codec.MsgDecoder;
import binary.wz.im.common.codec.MsgEncoder;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.connector.config.ConnectorConfig;
import binary.wz.im.connector.handler.ConnectorClientHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author binarywz
 * @date 2022/5/25 21:54
 * @description: Connector服务端，处理Client连接
 */
public class ConnectorServer {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorServer.class);

    public static void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("MsgDecoder", ConnectorConfig.injector.getInstance(MsgDecoder.class));
                        pipeline.addLast("MsgEncoder", ConnectorConfig.injector.getInstance(MsgEncoder.class));
                        pipeline.addLast("ConnectorClientHandler", ConnectorConfig.injector.getInstance(ConnectorClientHandler.class));
                    }
                });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(ConnectorConfig.port)).addListener(f -> {
            if (f.isSuccess()) {
                logger.info("[connector] start successfully at port {}, waiting for clients to connect...", ConnectorConfig.port);
            } else {
                throw new ImException("[connector] start failed");
            }
        });

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ImException("[connector] start failed", e);
        }
    }
}
