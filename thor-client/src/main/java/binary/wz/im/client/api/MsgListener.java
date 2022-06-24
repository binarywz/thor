package binary.wz.im.client.api;

import binary.wz.im.common.proto.Chat;
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
     * read msg
     * @param chatMsg
     */
    void read(Chat.ChatMsg chatMsg);

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
