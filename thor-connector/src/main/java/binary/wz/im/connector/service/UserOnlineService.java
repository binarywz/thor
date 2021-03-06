package binary.wz.im.connector.service;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.connector.config.ConnectorConfig;
import binary.wz.im.connector.context.ClientConn;
import binary.wz.im.connector.context.ClientConnContext;
import binary.wz.im.connector.context.ConnectorTransferContext;
import binary.wz.im.session.util.IdWorker;
import binary.wz.im.status.factory.UserStatusServiceFactory;
import binary.wz.im.status.service.UserStatusService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/5/2 23:36
 * @description:
 */
@Singleton
public class UserOnlineService {
    private final static Logger logger = LoggerFactory.getLogger(UserOnlineService.class);

    private ClientConnContext connContext;
    private UserStatusService statusService;
    private ConnectorTransferContext transferContext;

    @Inject
    public UserOnlineService(ClientConnContext connContext, ConnectorTransferContext transferContext,
                             UserStatusServiceFactory userStatusServiceFactory) {
        this.connContext = connContext;
        this.transferContext = transferContext;

        Properties properties = new Properties();
        properties.put("host", ConnectorConfig.redisHost);
        properties.put("port", ConnectorConfig.redisPort);
        properties.put("password", ConnectorConfig.redisPassword);
        this.statusService = userStatusServiceFactory.createService(properties);
    }

    /**
     * 构造用户连接
     * @param userId
     * @param ctx
     * @return
     */
    public ClientConn buildUserConn(String userId, ChannelHandlerContext ctx) {
        ClientConn conn = new ClientConn(ctx);
        conn.setUserId(userId);
        connContext.addConn(conn);
        return conn;
    }

    public ClientConn userOnline(String userId, ChannelHandlerContext ctx) {
        /**
         * TODO 客户端应直接拉取离线消息，拉取完再建立与Connector的连接，消息漫游
         */
        /**
         * TODO 此处后续要考虑实现同终端互踢
         */
        String oldConnectorId = statusService.online(userId, transferContext.getConnectorId());
        if (oldConnectorId != null) {
            // can't online twice
            sendErrorToClient("already online", ctx);
        }
        return connContext.getConnByUserId(userId);
    }

    private void sendErrorToClient(String errMsg, ChannelHandlerContext ctx) {
        Internal.InternalMsg errAck = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.UUID())
                .setVersion(MsgVersion.V1.getVersion())
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ERROR)
                .setMsgBody(errMsg)
                .build();
        ctx.writeAndFlush(errMsg);
    }

    /**
     * 处理用户下线
     * @param ctx
     */
    public void userOffline(ChannelHandlerContext ctx) {
        ClientConn conn = connContext.getConn(ctx);
        if (conn == null) {
            return;
        }
        // 删除缓存中用户对应的Connector
        statusService.offline(conn.getUserId());
        // 删除本地缓存中的连接
        connContext.removeConn(ctx);
    }
}
