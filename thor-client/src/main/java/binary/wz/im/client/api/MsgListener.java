package binary.wz.im.client.api;

import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Notify;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author binarywz
 * @date 2022/6/12 0:10
 * @description:
 */
public interface MsgListener {
    /**
     * do when the client connect to connector successfully
     */
    void online();

    /**
     * 为ChatMsg回复ACK消息
     * @param chatMsg
     */
    void confirmChatMsg(Chat.ChatMsg chatMsg);

    /**
     * 为NotifyMsg回复ACK消息
     * @param notifyMsg
     */
    void confirmNotifyMsg(Notify.NotifyMsg notifyMsg);

    /**
     * do when msg has been sent
     * @param mid
     */
    void hasSent(Long mid);

    /**
     * do when msg has been delivered
     * @param mid
     */
    void hasDelivered(Long mid);

    /**
     * do when msg has been read
     * @param mid
     */
    void hasRead(Long mid);

    /**
     * do when client disconnect to connector
     */
    void offline();

    /**
     * exception occurred
     * @param ctx
     */
    void hasException(ChannelHandlerContext ctx, Throwable cause);
}
