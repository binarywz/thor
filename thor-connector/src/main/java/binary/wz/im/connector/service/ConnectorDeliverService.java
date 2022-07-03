package binary.wz.im.connector.service;

import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.connector.context.ClientConnContext;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.session.conn.Conn;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/4/29 23:04
 * @description: Connector只提供通道，所有从Client过来的消息都会转发至Transfer
 */
@Singleton
public class ConnectorDeliverService {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorDeliverService.class);

    private ClientConnContext connContext;
    private ConnectorTransferContext transferContext;

    @Inject
    public ConnectorDeliverService(ClientConnContext connContext, ConnectorTransferContext transferContext) {
        this.connContext = connContext;
        this.transferContext = transferContext;
    }

    /**
     * 向Transfer转发消息
     * @param msg
     */
    public void doSendToTransfer(Message msg) {
        ChannelHandlerContext ctx = transferContext.getOneTransferCtx(System.currentTimeMillis());
        ctx.writeAndFlush(msg);
    }

    /**
     * 向Client发送消息
     * @param msg
     */
    public void doSendToClient(Message msg) {
        String destId = null;
        if (msg instanceof Chat.ChatMsg) {
            destId = ((Chat.ChatMsg) msg).getDestId();
        } else if (msg instanceof Notify.NotifyMsg) {
            destId = ((Notify.NotifyMsg) msg).getDestId();
        } else if (msg instanceof Internal.InternalMsg) {
            destId = ((Internal.InternalMsg) msg).getDestId();
        } else {
            logger.error("ConnectorDeliverService#doSendToClient msg not match: {}", msg.toString());
            return;
        }
        /**
         * 发送消息给destId
         * 发送消息: Chat.ChatMsg/Notify.NotifyMsg
         * 1.若destId对应的连接不在本台服务器(用户原本在此在Connector，Transfer消息转发过来的时候用户离线)，则将消息发送至Transfer进行转发
         * 2.若destId对应的连接在本台服务器，则将消息直接发送至对应用户
         */
        Conn conn = connContext.getConnByUserId(destId);
        if (conn == null) {
            ChannelHandlerContext ctx = transferContext.getOneTransferCtx(System.currentTimeMillis());
            ctx.writeAndFlush(msg);
            return;
        }
        conn.getCtx().writeAndFlush(msg);
    }
}
