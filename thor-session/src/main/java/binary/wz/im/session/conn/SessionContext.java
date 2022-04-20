package binary.wz.im.session.conn;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author binarywz
 * @date 2022/4/19 23:55
 * @description:
 */
public class SessionContext {
    /**
     * key: 连接Id，connectionId/netId
     * value: session中的消息ID
     */
    private static ConcurrentMap<Serializable, AtomicLong> sessionMap;

    static {
        sessionMap = new ConcurrentHashMap<>();
    }

    /**
     * consistent id
     * for ChatMsg, AckMsg
     *
     * @return
     */
    public static Long nextId(Serializable connectionId) {
        return sessionMap.computeIfAbsent(connectionId,
                key -> new AtomicLong(0)).incrementAndGet();
    }
}
