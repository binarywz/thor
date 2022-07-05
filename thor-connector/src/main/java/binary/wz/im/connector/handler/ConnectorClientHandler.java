package binary.wz.im.connector.handler;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.connector.context.ClientConn;
import binary.wz.im.connector.context.ClientConnContext;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.connector.service.ConnectorDeliverService;
import binary.wz.im.connector.service.UserOnlineService;
import binary.wz.im.session.ack.RcvAckWindow;
import binary.wz.im.session.ack.SndAckWindow;
import binary.wz.im.session.processor.AbstractMessageProcessor;
import binary.wz.im.session.processor.InternalMessageProcessor;
import binary.wz.im.session.util.IdWorker;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author binarywz
 * @date 2022/5/2 23:33
 * @description: 处理Client的消息，Connector不维护消息的可靠性，仅提供消息通道
 */
public class ConnectorClientHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private ConnectorDeliverService connectorDeliverService;
    private UserOnlineService userOnlineService;
    private ClientConnContext clientConnContext;
    private ConnectorTransferContext transferContext;
    private ClientMessageProcessor clientMessageProcessor;

    /**
     * 发送方ACK等待队列
     */
    private SndAckWindow sndAckWindow;
    /**
     * 接收方维护当前会话中收到的最后一个消息的id，lastId，防止消息重复/乱序
     */
    private RcvAckWindow rcvAckWindow;

    @Inject
    public ConnectorClientHandler(ConnectorDeliverService connectorDeliverService, UserOnlineService userOnlineService,
                                  ClientConnContext clientConnContext, ConnectorTransferContext transferContext) {
        this.connectorDeliverService = connectorDeliverService;
        this.userOnlineService = userOnlineService;
        this.clientConnContext = clientConnContext;
        this.transferContext = transferContext;
        this.clientConnContext = new ClientConnContext();
        this.clientMessageProcessor = new ClientMessageProcessor();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.info("ConnectorClientHandler#channelRead0 receive msg:\r\n{}", msg.toString());
        /**
         * 解析收到的消息，并根据不同的消息进行相应的处理
         */
        clientMessageProcessor.process(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOnlineService.userOffline(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[IM ConnectorClientHandler] has error: ", cause);
        clientConnContext.removeConn(ctx);
    }

    /**
     * 客户端消息处理器
     */
    class ClientMessageProcessor extends AbstractMessageProcessor {
        @Override
        public void registerProcessors() {
            InternalMessageProcessor internalMessageProcessor = new InternalMessageProcessor(3);
            /**
             * 注册Internal.InternalMsg.MsgType.GREET消息处理器:
             * 1.本地缓存userId与此台connector之间建立的连接
             * 2.初始化sndAckWindow/rcvAckWindow
             * 3.用户上线:在Redis中缓存userId:ConnectorTransferContext.connectorId的关系，connectorId全局范围内标识此台Connector
             * 4.为GREET消息响应ACK
             */
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.GREET, (m, ctx) -> {
                /**
                 * m.getMsgBody() -> userId，此处为本地缓存userId:connection之间的关系
                 */
                ClientConn conn = userOnlineService.buildUserConn(m.getMsgBody(), ctx);
                sndAckWindow = new SndAckWindow(conn.getNetId(), 10, Duration.ofSeconds(5));
                rcvAckWindow = new RcvAckWindow(5);
                /**
                 * 用户正式上线，在Redis中缓存userId:ConnectorTransferContext.connectorId的关系
                 * connectorId全局范围内标识此台Connector，同时也是其在IM系统中的全局ID
                 */
                userOnlineService.userOnline(m.getMsgBody(), ctx);
                /**
                 * 为GREET消息响应ACK，消息体为此条GREET消息的ID
                 */
                ctx.writeAndFlush(buildGreetAck(m.getId()));
            });

            /**
             * 注册ACK消息处理器: 类比TCP协议的ACK，ACK报文不会重传，丢失之后由对方重发对应的消息
             * 处理ACK等待队列中消息m对应的事件，Internal.InternalMsg.MsgType.ACK为destId应答给fromId的ACK消息
             */
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) -> connectorDeliverService.doSendToClient(m));

            /**
             * 处理收到的聊天消息
             */
            register(Chat.ChatMsg.class, (m, ctx) -> connectorDeliverService.doSendToTransfer(m));

            /**
             * 处理收到的通知消息,READ/DELIVERED消息
             */
            register(Notify.NotifyMsg.class, (m, ctx) -> connectorDeliverService.doSendToTransfer(m));

            /**
             * 处理收到的内部消息,GREET/ACK消息
             */
            register(Internal.InternalMsg.class, internalMessageProcessor.generateFun());
        }
    }

    private Internal.InternalMsg buildGreetAck(String mid) {
        return Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.UUID()) // ack消息丢失不会重传
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody(mid)
                .build();
    }
}
