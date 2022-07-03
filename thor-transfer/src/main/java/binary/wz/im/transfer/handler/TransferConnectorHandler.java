package binary.wz.im.transfer.handler;

import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.session.processor.AbstractMessageProcessor;
import binary.wz.im.session.processor.InternalMessageProcessor;
import binary.wz.im.transfer.context.ConnectorConnContext;
import binary.wz.im.transfer.service.TransferService;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/4/25 22:58
 * @description: Transfer针对Connector连接的处理器
 * TODO 没有消息收发窗口
 */
public class TransferConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(TransferConnectorHandler.class);

    private TransferService transferService;
    private ConnectorConnContext connContext;
    private ConnectorMessageProcessor messageProcessor;

    @Inject
    public TransferConnectorHandler(TransferService transferService, ConnectorConnContext connContext) {
        this.connContext = connContext;
        this.transferService = transferService;
        this.messageProcessor = new ConnectorMessageProcessor();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("TransferConnectorHandler#channelRead0 get msg: {}", msg.toString());
        messageProcessor.process(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connContext.removeConn(ctx);
    }

    class ConnectorMessageProcessor extends AbstractMessageProcessor {
        @Override
        public void registerProcessors() {
            InternalMessageProcessor internalMessageProcessor = new InternalMessageProcessor(3);
            internalMessageProcessor.register(Internal.InternalMsg.MsgType.GREET, (m, ctx) -> transferService.doGreet(m, ctx));

            register(Chat.ChatMsg.class, (m, ctx) -> transferService.doChat(m)); // 转发CHAT消息
            register(Notify.NotifyMsg.class, (m, ctx) -> transferService.doNotify(m)); // 转发NOTIFY消息
            register(Internal.InternalMsg.class, internalMessageProcessor.generateFun()); // 处理INTERNAL.GREET消息
        }
    }
}
