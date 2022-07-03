package binary.wz.im.client.api;

import binary.wz.im.client.context.UserContext;
import binary.wz.im.client.domain.Friend;
import binary.wz.im.client.handler.ClientConnectorHandler;
import binary.wz.im.client.service.ClientRestService;
import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.domain.po.RelationDetail;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.session.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author binarywz
 * @date 2022/6/12 22:10
 * @description:
 */
public class UserApi {
    private final static Logger logger = LoggerFactory.getLogger(UserApi.class);

    private ClientRestService clientRestService;
    private UserContext userContext;

    public UserApi(ClientRestService clientRestService, UserContext userContext) {
        this.clientRestService = clientRestService;
        this.userContext = userContext;
    }

    /**
     * 登录
     * @param username
     * @param pwd
     * @return
     */
    public UserInfo login(String username, String pwd) {
        UserInfo userInfo = clientRestService.login(username, pwd);

        assert userInfo.getId() != null;

        userContext.setUserId(userInfo.getId());
        userContext.setToken(userInfo.getToken());
        userContext.addRelations(userInfo.getRelations());
        return userInfo;
    }

    /**
     * 与Connector握手
     * @param userId
     */
    public void greetToConnector(String userId) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.UUID())
                .setVersion(MsgVersion.V1.getVersion())
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody(userId)
                .build();
        try {
            CompletableFuture future = getHandler().getSndAckWindow().offer(greet.getId(), greet,
                    m -> getHandler().getCtx().writeAndFlush(m));
            future.get(10, TimeUnit.SECONDS);
            logger.info("[client] client connect to server successfully");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ImException("[client] waiting for connector's response failed", e);
        }
    }

    private ClientConnectorHandler getHandler() {
        return userContext.getClientConnectorHandler();
    }

    public List<Friend> friends(String token) {
        return getFriends(clientRestService.friends(userContext.getUserId(), token), userContext.getUserId());
    }

    private List<Friend> getFriends(List<RelationDetail> relationDetails, String userId) {
        return relationDetails.stream().map(r -> {
            Friend friend = new Friend();
            if (r.getUserId1().equals(userId)) {
                friend.setUserId(r.getUserId2());
                friend.setUsername(r.getUsername2());
            } else {
                friend.setUserId(r.getUserId1());
                friend.setUsername(r.getUsername1());
            }
            friend.setEncryptKey(r.getEncryptKey());
            return friend;
        }).collect(Collectors.toList());
    }

    public Void logout(String token) {
        return clientRestService.logout(token);
    }
}
