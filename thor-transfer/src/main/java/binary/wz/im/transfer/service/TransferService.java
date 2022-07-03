package binary.wz.im.transfer.service;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.session.conn.Conn;
import binary.wz.im.transfer.context.ConnectorConn;
import binary.wz.im.session.util.IdWorker;
import binary.wz.im.transfer.context.ConnectorConnContext;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/4/25 23:00
 * @description: 转发CHAT/STATE消息，处理INTERNAL.GREET消息
 */
public class TransferService {
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private ConnectorConnContext connContext;

    @Inject
    public TransferService(ConnectorConnContext connContext) {
        this.connContext = connContext;
    }

    /**
     * 转发聊天消息
     * @param msg
     */
    public void doChat(Chat.ChatMsg msg) {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());
        Chat.ChatMsg copy = Chat.ChatMsg.newBuilder().mergeFrom(msg).setSeq(generateSeq(msg.getFromId(), msg.getDestId())).build();
        if (conn != null) {
            conn.getCtx().writeAndFlush(copy);
        } else {
            doOffline(msg);
        }
    }

    /**
     * 转发NOTIFY消息
     * @param msg
     */
    public void doNotify(Notify.NotifyMsg msg) {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());
        Notify.NotifyMsg copy = Notify.NotifyMsg.newBuilder().mergeFrom(msg).setSeq(generateSeq(msg.getFromId(), msg.getDestId())).build();
        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    /**
     * 生成序列号
     * @param fromId
     * @param destId
     */
    public Long generateSeq(String fromId, String destId) {
        return 1L;
    }

    /**
     * Connector greet to transfer
     * @param msg
     * @param ctx
     */
    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        /**
         * Connector与Transfer建立连接后会将全局唯一标识自身的connectorId通过Greet消息
         * 发送给Transfer，Transfer会在本地维护联系
         */
        ctx.channel().attr(Conn.NET_ID).set(msg.getMsgBody());
        ConnectorConn conn = new ConnectorConn(ctx);
        connContext.addConn(conn);
        // ACK消息丢失不需要重发
        ctx.writeAndFlush(buildInternalAck(msg.getId()));
    }

    private Internal.InternalMsg buildInternalAck(String mid) {
        return Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.UUID()) // ACK消息丢失不需要重发
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(mid)
                .build();
    }

    private void doOffline(Message msg) {
        logger.error("TransferService doOffline, msg: {}", msg);
    }
}
