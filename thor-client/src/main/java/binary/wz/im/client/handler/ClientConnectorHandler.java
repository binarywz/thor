package binary.wz.im.client.handler;

import binary.wz.im.client.api.MsgListener;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.session.processor.NotifyMessageProcessor;
import binary.wz.im.session.ack.RcvAckWindow;
import binary.wz.im.session.ack.SndAckWindow;
import binary.wz.im.session.processor.AbstractMessageProcessor;
import binary.wz.im.session.processor.InternalMessageProcessor;
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

    /**
     * 回复ACK消息
     * @param msg
     */
    public void confirmReceived(Internal.InternalMsg msg) {
        this.ctx.writeAndFlush(msg);
    }

    /**
     * 向Connector发送消息，并维护发送队列
     * @param connectionId
     * @param mid
     * @param msg
     */
    public void writeAndFlush(Serializable connectionId, String mid, Message msg) {
        SndAckWindow.offer(connectionId, mid, msg, m -> ctx.writeAndFlush(m))
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
            /**
             * 处理内部消息
             */
            InternalMessageProcessor internalMessageProcessor = new InternalMessageProcessor(3);
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) -> sndAckWindow.ack(m));
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.ERROR, (m, ctx) ->
                    logger.error("[client] get error from connector {}", m.getMsgBody()));
            register(Internal.InternalMsg.class, internalMessageProcessor.generateFun());

            /**
             * 处理通知消息
             */
            NotifyMessageProcessor notifyMessageProcessor = new NotifyMessageProcessor(2);
            notifyMessageProcessor.register(Notify.NotifyMsg.MsgType.DELIVERED, (m, ctx) ->
                    offerNotify(m, ignore -> msgListener.confirmNotifyMsg(m)));
            notifyMessageProcessor.register(Notify.NotifyMsg.MsgType.READ, (m, ctx) ->
                    offerNotify(m, ignore -> msgListener.confirmNotifyMsg(m)));
            register(Notify.NotifyMsg.class, notifyMessageProcessor.generateFun());

            /**
             * 处理聊天消息
             */
            register(Chat.ChatMsg.class, (m, ctx) ->
                    offerChat(m, ignore -> msgListener.confirmChatMsg(m)));
        }

        private void offerChat(Chat.ChatMsg m, Consumer<Message> consumer) {
            Chat.ChatMsg copy = Chat.ChatMsg.newBuilder().mergeFrom(m).build();
            offer(m.getId(), m.getSeq(), m.getDestId(), m.getFromId(), copy, consumer);
        }

        private void offerNotify(Notify.NotifyMsg m, Consumer<Message> consumer) {
            Notify.NotifyMsg copy = Notify.NotifyMsg.newBuilder().mergeFrom(m).build();
            offer(m.getId(), m.getSeq(), m.getDestId(), m.getFromId(), copy, consumer);
        }

        private void offer(String mid, Long seq, String fromId, String destId, Message m, Consumer<Message> consumer) {
            rcvAckWindow.offer(mid, seq, fromId, destId, ctx, m, consumer);
        }
    }
}
