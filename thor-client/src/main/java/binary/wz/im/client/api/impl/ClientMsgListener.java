package binary.wz.im.client.api.impl;

import binary.wz.im.client.api.ChatApi;
import binary.wz.im.client.api.MsgListener;
import binary.wz.im.client.domain.Friend;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Notify;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author binarywz
 * @date 2022/6/12 23:16
 * @description:
 */
public class ClientMsgListener implements MsgListener {
    private final static Logger logger = LoggerFactory.getLogger(ClientMsgListener.class);

    private ChatApi chatApi;
    private UserInfo userInfo;
    private Map<String, Friend> friendMap;

    public ClientMsgListener(ChatApi chatApi, UserInfo userInfo, Map<String, Friend> friendMap) {
        this.chatApi = chatApi;
        this.userInfo = userInfo;
        this.friendMap = friendMap;
    }

    @Override
    public void online() {
        logger.info("[client] i have connected to server!");
    }

    @Override
    public void confirmChatMsg(Chat.ChatMsg chatMsg) {
        // when it's confirmed that user has read this msg
        logger.info(friendMap.get(chatMsg.getFromId()).getUsername() + ": "
                + chatMsg.getMsgBody().toStringUtf8());
        chatApi.confirmChatMsg(chatMsg);
        // TODO MQ异步存储消息
    }

    @Override
    public void confirmNotifyMsg(Notify.NotifyMsg notifyMsg) {
        // when it's confirmed that user has read this msg
        logger.info(friendMap.get(notifyMsg.getFromId()).getUsername() + ": "
                + notifyMsg.getMsgBody());
        chatApi.confirmNotifyMsg(notifyMsg);
        // TODO MQ异步存储消息
    }

    @Override
    public void hasSent(Long id) {
        logger.info(String.format("msg {} has been sent", id));
    }

    @Override
    public void hasDelivered(Long id) {
        logger.info("msg {} has been delivered", id);
    }

    @Override
    public void hasRead(Long id) {
        logger.info("msg {} has been read", id);
    }

    @Override
    public void offline() {
        logger.info("[{}] I am offline!", userInfo != null ? userInfo.getUsername() : "client");
    }

    @Override
    public void hasException(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[" + userInfo.getUsername() + "] has error ", cause);
    }
}
