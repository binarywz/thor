package binary.wz.im.client;

import binary.wz.im.client.api.ChatApi;
import binary.wz.im.client.api.MsgListener;
import binary.wz.im.client.api.UserApi;
import binary.wz.im.client.api.impl.ClientMsgListener;
import binary.wz.im.client.config.ClientConfig;
import binary.wz.im.client.context.UserContext;
import binary.wz.im.client.domain.Friend;
import binary.wz.im.client.handler.ClientConnectorHandler;
import binary.wz.im.client.handler.codec.AesDecoder;
import binary.wz.im.client.handler.codec.AesEncoder;
import binary.wz.im.client.service.ClientRestService;
import binary.wz.im.common.codec.MsgDecoder;
import binary.wz.im.common.codec.MsgEncoder;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.session.util.IdWorker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author binarywz
 * @date 2022/6/12 22:48
 * @description:
 */
public class ImClient {
    private final static Logger logger = LoggerFactory.getLogger(ImClient.class);

    public final String connectionId = IdWorker.UUID();
    private UserInfo userInfo;
    private Map<String, Friend> friendMap;

    private ChatApi chatApi;
    private UserApi userApi;
    private MsgListener msgListener;
    private UserContext userContext;
    private ClientConnectorHandler handler;

    public void start(String username, String pwd) {
        this.userContext = ClientConfig.injector.getInstance(UserContext.class);
        this.userApi = initUserApi();
        this.userInfo = login(username, pwd);
        this.friendMap = getFriends(this.userInfo.getToken());
        this.msgListener = new ClientMsgListener(new ChatApi(this.connectionId, this.userContext), this.userInfo, this.friendMap);
        this.handler = new ClientConnectorHandler(this.msgListener, this.connectionId);
        this.userContext.setClientConnectorHandler(this.handler);
        this.chatApi = initChatApi();

        // 客户端启动
        this.start(this.handler);
        // 跟Connector握手
        this.userApi.greetToConnector(this.userInfo.getId());
    }

    private void start(ClientConnectorHandler handler) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture future = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        // out
                        pipeline.addLast("MsgEncoder", new MsgEncoder());
                        pipeline.addLast("AesEncoder", new AesEncoder(userContext));

                        // in
                        pipeline.addLast("MsgDecoder", new MsgDecoder());
                        pipeline.addLast("AesDecoder", new AesDecoder(userContext));
                        pipeline.addLast("ClientConnectorHandler", handler);
                    }
                }).connect(ClientConfig.connectorHost, ClientConfig.connectorPort)
                .addListener(f -> {
                    if (f.isSuccess()) {
                        logger.info("ImClient connect to connector successfully");
                    } else {
                        throw new ImException("[client] connect to connector failed! connector url: "
                                + ClientConfig.connectorHost + ":" + ClientConfig.connectorPort);
                    }
                });
        try {
            future.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ImException("[client] connect to connector failed!");
        }
    }

    public void printUserInfo() {
        System.out.println("id: " + this.userInfo.getId());
        System.out.println("username: " + this.userInfo.getUsername());
    }

    public void send(String id, String text) {
        if (!this.friendMap.containsKey(id)) {
            System.out.println("friend " + id + " not found!");
            return;
        }
        this.chatApi.text(id, text);
    }

    private UserApi initUserApi() {
        return new UserApi(ClientConfig.injector.getInstance(ClientRestService.class), this.userContext);
    }

    private ChatApi initChatApi() {
        return new ChatApi(this.connectionId, this.userContext);
    }

    private UserInfo login(String username, String pwd) {
        return this.userApi.login(username, DigestUtils.sha256Hex(pwd.getBytes(CharsetUtil.UTF_8)));
    }

    private Map<String, Friend> getFriends(String token) {
        List<Friend> friends = this.userApi.friends(token);
        listFriend(friends);
        return friends.stream().collect(Collectors.toMap(Friend::getUserId, f -> f));
    }

    private void listFriend(List<Friend> friends) {
        System.out.println("Here are my friends!");
        for (Friend friend : friends) {
            System.out.println(friend.getUserId() + ": " + friend.getUsername());
        }
    }
}
