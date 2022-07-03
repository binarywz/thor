package binary.wz.im.client.api;

import binary.wz.im.client.context.UserContext;
import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.session.util.IdWorker;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import java.nio.charset.StandardCharsets;

/**
 * @author binarywz
 * @date 2022/6/12 20:50
 * @description:
 */
public class ChatApi {

    private final String connectionId;
    private UserContext userContext;

    public ChatApi(String connectionId, UserContext userContext) {
        this.connectionId = connectionId;
        this.userContext = userContext;
    }

    /**
     * 发送文本消息
     * @param destId
     * @param text
     * @return
     */
    public String text(String destId, String text) {
        validateLogin();
        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setId(IdWorker.UUID())
                .setFromId(userContext.getUserId())
                .setDestId(destId)
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom(text, StandardCharsets.UTF_8))
                .build();
        this.sendToConnector(chat.getId(), chat);
        return chat.getId();
    }

    private void validateLogin() {
        if (userContext.getUserId() == null) {
            throw new ImException("client has not login!");
        }
    }

    /**
     * 向Connector发送消息，并维护发送队列
     * @param mid
     * @param msg
     */
    private void sendToConnector(String mid, Message msg) {
        userContext.getClientConnectorHandler().writeAndFlush(connectionId, mid, msg);
    }

    /**
     * 为ChatMsg回复ACK消息
     * @param msg
     */
    public void confirmChatMsg(Chat.ChatMsg msg) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.UUID())
                .setSeq(msg.getSeq()) // seq是收到的消息的序列号，ACK消息没有自己的序列号
                .setVersion(MsgVersion.V1.getVersion())
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(msg.getId())
                .build();
        this.confirmReceived(ack);
    }

    /**
     * 为ChatMsg回复ACK消息
     * @param msg
     */
    public void confirmNotifyMsg(Notify.NotifyMsg msg) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.UUID())
                .setSeq(msg.getSeq()) // seq是收到的消息的序列号，ACK消息没有自己的序列号
                .setVersion(MsgVersion.V1.getVersion())
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(msg.getId())
                .build();
        this.confirmReceived(ack);
    }

    /**
     * 回复ACK消息
     * @param msg
     */
    private void confirmReceived(Internal.InternalMsg msg) {
        userContext.getClientConnectorHandler().confirmReceived(msg);
    }
}
