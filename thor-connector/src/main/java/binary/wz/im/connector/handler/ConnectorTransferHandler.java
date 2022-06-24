package binary.wz.im.connector.handler;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.parser.MessageParser;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.State;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.connector.service.ConnectorDeliverService;
import binary.wz.im.session.ack.SndAckWindow;
import binary.wz.im.session.conn.Conn;
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
 * @date 2022/4/29 22:55
 * @description: 处理Transfer的消息
 */
public class ConnectorTransferHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorTransferHandler.class);

    private SndAckWindow sndAckWindow;
    private TransferMessageProcessor transferMessageProcessor;
    private ConnectorDeliverService connectorDeliverService;
    private ConnectorTransferContext transferContext;

    @Inject
    public ConnectorTransferHandler(ConnectorDeliverService connectorDeliverService, ConnectorTransferContext transferContext) {
        this.connectorDeliverService = connectorDeliverService;
        this.transferContext = transferContext;
        this.transferMessageProcessor = new TransferMessageProcessor();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("ConnectorTransferHandler#channelActive connect to transfer");

        setConnectionId(ctx);
        sndAckWindow = new SndAckWindow(IdWorker.UUID(), 10, Duration.ofSeconds(5));
        greetToTransfer(ctx);

        transferContext.addTransferCtx(ctx);
    }

    private void greetToTransfer(ChannelHandlerContext ctx) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.snowGenId())
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody(transferContext.getConnectorId())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.TRANSFER)
                .setCreateTime(System.currentTimeMillis())
                .build();
        sndAckWindow.offer(greet.getId(), greet, ctx::writeAndFlush)
                .thenAccept(m -> logger.info("ConnectorTransferHandler#greetToTransfer connect to transfer successfully"))
                .exceptionally(e -> {
                    logger.error("ConnectorTransferHandler#greetToTransfer connect to transfer failed");
                    return null;
                });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("ConnectorTransferHandler#channelRead0 receive msg:\r\n{}", msg.toString());

        MessageParser.validateFrom(msg, Internal.InternalMsg.Module.TRANSFER);
        MessageParser.validateDest(msg, Internal.InternalMsg.Module.CONNECTOR);

        transferMessageProcessor.process(msg, ctx);
    }

    class TransferMessageProcessor extends AbstractMessageProcessor {
        @Override
        public void registerProcessors() {
            InternalMessageProcessor internalMessageProcessor = new InternalMessageProcessor(3);
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) -> sndAckWindow.ack(m));

            register(Chat.ChatMsg.class, (m, ctx) -> connectorDeliverService.doChatToClient(m));
            register(State.StateMsg.class, (m, ctx) -> connectorDeliverService.doSendStateToClient(m));
            register(Internal.InternalMsg.class, internalMessageProcessor.generateFun());
        }
    }

    /**
     * 设置当前连接的connectionId
     * @param ctx
     */
    private void setConnectionId(ChannelHandlerContext ctx) {
        ctx.channel().attr(Conn.NET_ID).set(IdWorker.UUID());
    }
}
