package binary.wz.im.connector.remoting;

import binary.wz.im.common.codec.MsgDecoder;
import binary.wz.im.common.codec.MsgEncoder;
import binary.wz.im.common.constant.Constants;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.connector.config.ConnectorConfig;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.connector.handler.ConnectorTransferHandler;
import binary.wz.im.registry.NotifyListener;
import binary.wz.im.registry.domain.RegistryConfig;
import binary.wz.im.registry.domain.ServiceConfig;
import binary.wz.im.registry.zookeeper.ZookeeperRegistryService;
import com.google.inject.Inject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author binarywz
 * @date 2022/5/25 22:06
 * @description: Connector客户端，连接Transfer进行消息转发
 */
public class ConnectorClient implements NotifyListener {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorClient.class);

    private final Map<String, EventLoopGroup> groupMap;
    private final Map<String, ConnectorTransferHandler> handlerMap;
    private final ConnectorTransferContext transferContext;
    private final Lock NOTIFY_LOCK = new ReentrantLock();

    @Inject
    public ConnectorClient(ConnectorTransferContext transferContext) {
        this.groupMap = new ConcurrentHashMap<>();
        this.handlerMap = new ConcurrentHashMap<>();
        this.transferContext = transferContext;
    }

    public void start() {
        RegistryConfig registryConfig = new RegistryConfig(ConnectorConfig.registryAddress);
        ServiceConfig serviceConfig = new ServiceConfig(ConnectorConfig.serviceGroup, Constants.CONNECTOR);
        ZookeeperRegistryService registryService = new ZookeeperRegistryService(registryConfig, serviceConfig);
        List<ServiceConfig> configs = registryService.lookup();
        if (configs == null || configs.isEmpty()) {
            logger.error("ConnectorClient startup failed...");
            return;
        }
        for (ServiceConfig config : configs) {
            logger.info("ServiceConfig: {}", config.toString());
            connect(config);
        }
        registryService.subscribe(this);
    }

    private synchronized void connect(ServiceConfig config) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ConnectorTransferHandler transferHandler = ConnectorConfig.injector.getInstance(ConnectorTransferHandler.class);
        ChannelFuture future = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("MsgDecoder", ConnectorConfig.injector.getInstance(MsgDecoder.class));
                        pipeline.addLast("MsgEncoder", ConnectorConfig.injector.getInstance(MsgEncoder.class));
                        pipeline.addLast("ConnectorTransferHandler", transferHandler);
                    }
                }).connect(config.getHost(), config.getPort())
                .addListener(f -> {
                    if (!f.isSuccess()) {
                        throw new ImException("[connector] connect to transfer failed! transfer address: " + config.getKey());
                    }
                });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ImException("[connector] connect to transfer failed! transfer address: " + config.getKey(), e);
        }
        this.groupMap.put(config.getKey(), group);
        this.handlerMap.put(config.getKey(), transferHandler);
    }

    @Override
    public void notify(ServiceConfig config) {
        logger.warn("[ConnectorClient] notify, config: {}", config.toString());
        if (groupMap.containsKey(config.getKey()) && config.getStatus() == Constants.SERVICE_ONLINE) {
            return;
        }
        if (groupMap.containsKey(config.getKey()) && config.getStatus() == Constants.SERVICE_OFFLINE) {
            NOTIFY_LOCK.lock();
            try {
                transferContext.removeTransferCtx(handlerMap.get(config.getKey()).getTransferCtx());
                groupMap.get(config.getKey()).shutdownGracefully();
                return;
            } finally {
                NOTIFY_LOCK.unlock();
            }
        }
        connect(config);
    }
}
