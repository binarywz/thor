package binary.wz.im.client.handler;

import binary.wz.im.client.api.MsgListener;
import binary.wz.im.common.parser.MessageParser;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.State;
import binary.wz.im.session.ack.RcvAckWindow;
import binary.wz.im.session.ack.SndAckWindow;
import binary.wz.im.session.processor.AbstractMessageProcessor;
import binary.wz.im.session.processor.InternalMessageProcessor;
import binary.wz.im.session.processor.StateMessageProcessor;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/6/10 0:12
 * @description:
 */
public class ClientConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(ClientConnectorHandler.class);

    private MsgListener msgListener;
    private ConnectorMessageProcessor connectorMessageProcessor;
    private ChannelHandlerContext ctx;
    private String connectionId;

    /**
     * ACK维护队列
     */
    private SndAckWindow sndAckWindow;
    private RcvAckWindow rcvAckWindow;

    public ClientConnectorHandler(MsgListener msgListener) {
        assert msgListener != null;

        this.msgListener = msgListener;
        this.connectorMessageProcessor = new ConnectorMessageProcessor();
    }

    public ClientConnectorHandler(MsgListener msgListener, String connectionId) {
        assert msgListener != null;

        this.connectionId = connectionId;
        this.msgListener = msgListener;
        this.connectorMessageProcessor = new ConnectorMessageProcessor();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        this.sndAckWindow = new SndAckWindow(this.connectionId, 10, Duration.ofSeconds(5));
        this.rcvAckWindow = new RcvAckWindow(5);
        msgListener.online();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[client] get msg:\r\n{}", msg.toString());

        MessageParser.validateFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        MessageParser.validateDest(msg, Internal.InternalMsg.Module.CLIENT);

        connectorMessageProcessor.process(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("[client] disconnect to connector");
        msgListener.offline();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[client] has error: ", cause);
        msgListener.hasException(ctx, cause);
    }

    public void writeAndFlush(Serializable connectionId, Long mid, Message msg) {
        SndAckWindow.offer(connectionId, mid, msg, m -> ctx.writeAndFlush(m))
                .thenAccept(m -> msgListener.hasSent(mid))
                .exceptionally(e -> {
                    logger.error("[client] send to connector failed", e);
                    return null;
                });
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public SndAckWindow getSndAckWindow() {
        return this.sndAckWindow;
    }

    class ConnectorMessageProcessor extends AbstractMessageProcessor {

        @Override
        public void registerProcessors() {
            InternalMessageProcessor internalMessageProcessor = new InternalMessageProcessor(3);
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) -> sndAckWindow.ack(m));
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ERROR, (m, ctx) ->
                    logger.error("[client] get error from connector {}", m.getMsgBody()));
            register(Internal.InternalMsg.class, internalMessageProcessor.generateFun());

            StateMessageProcessor stateMessageProcessor = new StateMessageProcessor(2);
            stateMessageProcessor.register(State.StateMsg.MsgType.DELIVERED, (m, ctx) ->
                    offerState(m.getId(), m, ignore -> msgListener.hasDelivered(m.getStateMsgId())));
            stateMessageProcessor.register(State.StateMsg.MsgType.READ, (m, ctx) ->
                    offerState(m.getId(), m, ignore -> msgListener.hasRead(m.getStateMsgId())));
            register(State.StateMsg.class, stateMessageProcessor.generateFun());

            /**
             * TODO 此处客户端收到消息后立马回复了一条READ消息，后续改为回复ACK消息
             */
            register(Chat.ChatMsg.class, (m, ctx) ->
                    offerChat(m.getId(), m, ignore -> msgListener.read(m)));
        }

        private void offerChat(Long mid, Chat.ChatMsg m, Consumer<Message> consumer) {
            Chat.ChatMsg copy = Chat.ChatMsg.newBuilder().mergeFrom(m).build();
            offer(mid, copy, consumer);
        }

        private void offerState(Long mid, State.StateMsg m, Consumer<Message> consumer) {
            State.StateMsg copy = State.StateMsg.newBuilder().mergeFrom(m).build();
            offer(mid, copy, consumer);
        }

        private void offer(Long mid, Message m, Consumer<Message> consumer) {
            rcvAckWindow.offer(mid,
                    Internal.InternalMsg.Module.CLIENT,
                    Internal.InternalMsg.Module.CONNECTOR,
                    ctx, m, consumer);
        }
    }
}
