package binary.wz.im.client.api;

import binary.wz.im.client.context.UserContext;
import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.State;
import binary.wz.im.session.context.SessionContext;
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

    public Long text(String destId, String text) {
        validateLogin();

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setId(SessionContext.nextId(connectionId))
                .setFromId(userContext.getUserId())
                .setDestId(destId)
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom(text, StandardCharsets.UTF_8))
                .build();

        sendToConnector(chat.getId(), chat);
        return chat.getId();
    }

    private void validateLogin() {
        if (userContext.getUserId() == null) {
            throw new ImException("client has not login!");
        }
    }

    private void sendToConnector(Long mid, Message msg) {
        userContext.getClientConnectorHandler().writeAndFlush(connectionId, mid, msg);
    }

    public void confirmRead(Chat.ChatMsg msg) {
        State.StateMsg read = State.StateMsg.newBuilder()
                .setId(SessionContext.nextId(connectionId))
                .setVersion(MsgVersion.V1.getVersion())
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setCreateTime(System.currentTimeMillis())
                .setDestType(msg.getDestType() == Chat.ChatMsg.DestType.SINGLE ? State.StateMsg.DestType.SINGLE : State.StateMsg.DestType.GROUP)
                .setMsgType(State.StateMsg.MsgType.READ)
                .setStateMsgId(msg.getId())
                .build();
        this.sendToConnector(read.getId(), read);
    }
}
