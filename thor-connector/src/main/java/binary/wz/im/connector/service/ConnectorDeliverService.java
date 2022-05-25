package binary.wz.im.connector.service;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.State;
import binary.wz.im.connector.context.ClientConn;
import binary.wz.im.connector.context.ClientConnContext;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.session.ack.SndAckWindow;
import binary.wz.im.session.conn.Conn;
import binary.wz.im.session.context.SessionContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author binarywz
 * @date 2022/4/29 23:04
 * @description:
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
     * 发送聊天消息
     * TODO 函数体可进一步抽象
     * @param msg
     */
    public void doChatToClient(Chat.ChatMsg msg) {
        /**
         * 发送消息给destId
         * 1.cid为当前机器上的connectionId，标识对应的session
         * 2.SessionContext.nextId(cid): 计算对应session消息的id，mid=lastMid+1
         */
        boolean onThisMachine = sendMessage(msg.getDestId(),
                cid -> Chat.ChatMsg.newBuilder().mergeFrom(msg).setId(SessionContext.nextId(cid)).build());
        /**
         * 发送STATE消息给fromId
         * TODO 需分析在此发送Deliver消息的优缺点
         */
        if (onThisMachine) {
            // 从本地缓存中获取fromId对应的连接
            ClientConn conn = connContext.getConnByUserId(msg.getFromId());
            if (conn == null) {
                ChannelHandlerContext ctx = transferContext.getOneTransferCtx(System.currentTimeMillis());
                ctx.writeAndFlush(buildDeliveredMsg(ctx.channel().attr(Conn.NET_ID).get(), msg));
            } else {
                State.StateMsg delivered = buildDeliveredMsg(conn.getNetId(), msg);
                SndAckWindow.offer(conn.getNetId(), delivered.getId(), delivered, m -> conn.getCtx().writeAndFlush(m));
            }
        }
    }

    /**
     * 发送msg的状态消息
     * @param msg
     */
    public void doSendStateToClient(State.StateMsg msg) {
        sendMessage(msg.getDestId(),
                cid -> State.StateMsg.newBuilder().mergeFrom(msg).setId(SessionContext.nextId(cid)).build());
    }

    /**
     * 构造已投递消息
     * @param connectionId
     * @param msg
     * @return
     */
    private State.StateMsg buildDeliveredMsg(Serializable connectionId, Chat.ChatMsg msg) {
        return State.StateMsg.newBuilder()
                .setId(SessionContext.nextId(connectionId)) // STATE消息占用消息Id，connectionId对应的session消息Id+1
                .setVersion(MsgVersion.V1.getVersion())
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setDestType(msg.getDestType() == Chat.ChatMsg.DestType.SINGLE ? State.StateMsg.DestType.SINGLE : State.StateMsg.DestType.GROUP)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(State.StateMsg.MsgType.DELIVERED)
                .setStateMsgId(msg.getId()) // 对应消息的状态
                .build();
    }

    /**
     * 发送消息: Chat.ChatMsg/State.StateMsg
     * 1.若destId对应的连接不在本台服务器，则将消息发送至Transfer进行转发，返回false
     * 2.若destId对应的连接在本台服务器，则将消息直接发送至对应用户，返回true
     * @param destId
     * @param generateMsg
     * @return
     */
    private boolean sendMessage(String destId, Function<Serializable, Message> generateMsg) {
        // 从本地缓存中获取destId对应的连接
        Conn conn = connContext.getConnByUserId(destId);
        if (conn == null) {
            ChannelHandlerContext ctx = transferContext.getOneTransferCtx(System.currentTimeMillis());
            ctx.writeAndFlush(generateMsg.apply(ctx.channel().attr(Conn.NET_ID).get()));
            return false;
        }
        Message msg = generateMsg.apply(conn.getNetId());
        Long mid = null;
        if (msg instanceof Chat.ChatMsg) {
            Chat.ChatMsg chatMsg = (Chat.ChatMsg) msg;
            mid = chatMsg.getId();
        } else {
            State.StateMsg stateMsg = (State.StateMsg) msg;
            mid = stateMsg.getId();
        }
        SndAckWindow.offer(conn.getNetId(), mid, msg, m -> conn.getCtx().writeAndFlush(m));
        return true;
    }
}
