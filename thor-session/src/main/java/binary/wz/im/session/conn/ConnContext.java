package binary.wz.im.session.conn;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2022/4/18 23:47
 * @description: 存储连接的容器
 */
public interface ConnContext<T extends Conn> {

    /**
     * 获取连接
     *
     * @param ctx
     * @return
     */
    T getConn(ChannelHandlerContext ctx);

    /**
     * 获取连接
     *
     * @param netId
     * @return
     */
    T getConn(Serializable netId);

    /**
     * 添加连接
     *
     * @param conn
     */
    void addConn(T conn);

    /**
     * 删除连接
     *
     * @param ctx
     */
    void removeConn(ChannelHandlerContext ctx);

    /**
     * 删除连接
     *
     * @param netId
     */
    void removeConn(Serializable netId);

    /**
     * 删除所有连接
     */
    void removeAllConn();

}
