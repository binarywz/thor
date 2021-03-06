package binary.wz.im.transfer.context;

import binary.wz.im.session.conn.AbstractConn;
import binary.wz.im.session.conn.Conn;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2022/4/18 23:52
 * @description:
 */
public class ConnectorConn extends AbstractConn {

    public ConnectorConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(Conn.NET_ID).get();
    }
}
