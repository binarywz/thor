package binary.wz.im.session.context;

import binary.wz.im.session.conn.Conn;
import com.google.inject.Singleton;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author binarywz
 * @date 2022/4/18 23:58
 * @description: 本地连接缓存
 */
@Singleton
public class MemoryConnContext<T extends Conn> implements ConnContext<T> {
    private static final Logger logger = LoggerFactory.getLogger(MemoryConnContext.class);

    protected ConcurrentHashMap<Serializable, T> connMap;

    public MemoryConnContext() {
        this.connMap = new ConcurrentHashMap<>();
    }

    @Override
    public T getConn(ChannelHandlerContext ctx) {
        Serializable netId = ctx.channel().attr(Conn.NET_ID).get();
        if (netId == null) {
            logger.warn("Conn netId not found in ctx: {}", ctx.toString());
            return null;
        }
        T conn = connMap.get(netId);
        if (conn == null) {
            logger.warn("Conn not found, netId: {}", netId);
        }
        return conn;
    }

    @Override
    public T getConn(Serializable netId) {
        T conn = connMap.get(netId);
        if (conn == null) {
            logger.warn("Conn not found, netId: {}", netId);
        }
        return conn;
    }

    @Override
    public void addConn(T conn) {
        logger.debug("add a conn, netId: {}", conn.getNetId());
        connMap.put(conn.getNetId(), conn);
    }

    @Override
    public void removeConn(ChannelHandlerContext ctx) {
        Serializable netId = ctx.channel().attr(Conn.NET_ID).get();
        if (netId == null) {
            logger.warn("Can't find a netId for the ctx.");
        } else {
            removeConn(netId);
        }
    }

    @Override
    public void removeConn(Serializable netId) {
        // TODO 考虑并发
        connMap.computeIfPresent(netId, (id, conn) -> {
            conn.close();
            return null;
        });
        connMap.remove(netId);
    }

    @Override
    public void removeAllConn() {
        // TODO 考虑并发
        connMap.values().stream().parallel().close();
        connMap.clear();
    }
}
