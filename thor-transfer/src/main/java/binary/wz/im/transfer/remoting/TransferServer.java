package binary.wz.im.transfer.remoting;

import binary.wz.im.common.codec.MsgDecoder;
import binary.wz.im.common.codec.MsgEncoder;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.transfer.config.TransferConfig;
import binary.wz.im.transfer.handler.TransferConnectorHandler;
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
 * @date 2022/4/27 22:38
 * @description:
 */
public class TransferServer {
    private static Logger logger = LoggerFactory.getLogger(TransferServer.class);

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
                        pipeline.addLast("MsgDecoder", TransferConfig.injector.getInstance(MsgDecoder.class));
                        pipeline.addLast("MsgEncoder", TransferConfig.injector.getInstance(MsgEncoder.class));
                        pipeline.addLast("TransferConnectorHandler", TransferConfig.injector.getInstance(TransferConnectorHandler.class));
                    }
                });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(TransferConfig.port)).addListener(f -> {
            if (f.isSuccess()) {
                logger.info("[transfer] start successful at port {}, waiting for connectors to connect...", TransferConfig.port);
            } else {
                throw new ImException("[transfer] start failed");
            }
        });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            throw new ImException("[transfer] start failed");
        }
    }
}
