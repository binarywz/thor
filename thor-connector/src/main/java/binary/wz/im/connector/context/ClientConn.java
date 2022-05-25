package binary.wz.im.connector.context;

import binary.wz.im.session.conn.AbstractConn;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author binarywz
 * @date 2022/4/28 23:53
 * @description:
 */
public class ClientConn extends AbstractConn {

    /**
     * 静态Generator，所有的ClientConn由此生成NetId
     */
    private final static AtomicLong NETID_GENERATOR = new AtomicLong(0);

    private String userId;

    public ClientConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return NETID_GENERATOR.getAndIncrement();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
