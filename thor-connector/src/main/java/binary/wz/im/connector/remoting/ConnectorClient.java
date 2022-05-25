package binary.wz.im.connector.remoting;

import binary.wz.im.common.codec.MsgDecoder;
import binary.wz.im.common.codec.MsgEncoder;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.connector.config.ConnectorConfig;
import binary.wz.im.connector.handler.ConnectorTransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author binarywz
 * @date 2022/5/25 22:06
 * @description: Connector客户端，连接Transfer进行消息转发
 */
public class ConnectorClient {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorClient.class);

    public static void start() {
        for (String transferAddress : ConnectorConfig.transferUrls) {
            String[] address = transferAddress.split(":");

            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture future = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("MsgDecoder", ConnectorConfig.injector.getInstance(MsgDecoder.class));
                            pipeline.addLast("MsgEncoder", ConnectorConfig.injector.getInstance(MsgEncoder.class));
                            pipeline.addLast("ConnectorTransferHandler", ConnectorConfig.injector.getInstance(ConnectorTransferHandler.class));
                        }
                    }).connect(address[0], Integer.parseInt(address[1]))
                    .addListener(f -> {
                        if (!f.isSuccess()) {
                            throw new ImException("[connector] connect to transfer failed! transfer address: " + transferAddress);
                        }
                    });
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new ImException("[connector] connect to transfer failed! transfer address: " + transferAddress, e);
            }
        }
    }
}
