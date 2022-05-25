package binary.wz.im.connector.context;

import binary.wz.im.session.context.MemoryConnContext;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author binarywz
 * @date 2022/4/28 23:58
 * @description:
 */
@Singleton
public class ClientConnContext extends MemoryConnContext<ClientConn> {
    private final static Logger logger = LoggerFactory.getLogger(ClientConn.class);

    private ConcurrentHashMap<String, Serializable> userNetIdMap;

    public ClientConnContext() {
        this.connMap = new ConcurrentHashMap<>();
        this.userNetIdMap = new ConcurrentHashMap<>();
    }

    public ClientConn getConnByUserId(String userId) {
        logger.debug("ClientConnContext#getConnByUserId userId: {}", userId);

        Serializable netId = userNetIdMap.get(userId);
        if (netId == null) {
            logger.debug("ClientConnContext#getConnByUserId netId not exist");
            return null;
        }
        ClientConn conn = connMap.get(netId);
        if (conn == null) {
            logger.debug("ClientConnContext#getConnByUserId conn not exist");
            userNetIdMap.remove(userId);
            return null;
        }
        logger.debug("ClientConnContext#getConnByUserId found conn, userId:{}, connId: {}", userId, conn.getNetId());
        return conn;
    }

    @Override
    public void addConn(ClientConn conn) {
        String userId = conn.getUserId();
        if (userNetIdMap.containsKey(userId)) {
            removeConn(userNetIdMap.get(userId));
        }
        logger.debug("ClientConnContext#addConn userId: {}, netId: {}", userId, conn.getNetId());
        connMap.putIfAbsent(conn.getNetId(), conn);
        userNetIdMap.put(conn.getUserId(), conn.getNetId());
    }
}
