package binary.wz.im.transfer.context;

import binary.wz.im.session.context.MemoryConnContext;
import binary.wz.im.status.factory.UserStatusServiceFactory;
import binary.wz.im.status.service.UserStatusService;
import binary.wz.im.transfer.config.TransferConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/4/24 23:35
 * @description: 1.维护Transfer跟Connector之间的连接，ConnectorId是全局唯一的
 * 2.根据userId获取对应的ConnectorConn
 */
@Singleton
public class ConnectorConnContext extends MemoryConnContext<ConnectorConn> {

    private UserStatusService userStatusService;

    @Inject
    public ConnectorConnContext(UserStatusServiceFactory userStatusServiceFactory) {
        Properties properties = new Properties();
        properties.put("host", TransferConfig.redisHost);
        properties.put("port", TransferConfig.redisPort);
        properties.put("password", TransferConfig.redisPassword);
        this.userStatusService = userStatusServiceFactory.createService(properties);
    }

    /**
     * 根据userId获取对应的ConnectorConn
     * @param userId
     * @return
     */
    public ConnectorConn getConnByUserId(String userId) {
        // 从redis中获取userId对应的connectorId
        String connectorId = userStatusService.getConnectorId(userId);
        if (connectorId != null) {
            // 根据connectorId获取本地内存中的ConnectorConn
            ConnectorConn conn = getConn(connectorId);
            if (conn != null) {
                return conn;
            } else {
                // connectorId已过时，而用户还没再次上线
                userStatusService.offline(userId);
            }
        }
        return null;
    }
}
