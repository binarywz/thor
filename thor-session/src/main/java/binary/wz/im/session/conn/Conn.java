package binary.wz.im.session.conn;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2022/4/18 23:40
 * @description: 连接
 */
public interface Conn {

    AttributeKey<Serializable> NET_ID = AttributeKey.valueOf("netId");

    /**
     * 获取连接id
     *
     * @return
     */
    Serializable getNetId();

    /**
     * 获取ChannelHandlerContext
     *
     * @return
     */
    ChannelHandlerContext getCtx();

    /**
     * 关闭连接
     *
     * @return
     */
    ChannelFuture close();

}
