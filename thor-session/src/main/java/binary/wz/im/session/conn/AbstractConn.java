package binary.wz.im.session.conn;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2022/4/18 23:43
 * @description: 抽象连接
 */
public abstract class AbstractConn implements Conn {

    private Serializable netId;
    private ChannelHandlerContext ctx;

    public AbstractConn(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.netId = generateNetId(ctx);
        this.ctx.channel().attr(Conn.NET_ID).set(netId); // 自定义netId业务参数
    }

    /**
     * 生成连接ID
     *
     * @param ctx
     * @return
     */
    protected abstract Serializable generateNetId(ChannelHandlerContext ctx);

    @Override
    public Serializable getNetId() {
        return netId;
    }

    @Override
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    public ChannelFuture close() {
        return ctx.close();
    }
}
