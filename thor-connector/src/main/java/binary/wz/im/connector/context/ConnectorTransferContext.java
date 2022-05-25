package binary.wz.im.connector.context;

import binary.wz.im.session.util.IdWorker;
import com.google.inject.Singleton;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author binarywz
 * @date 2022/4/29 23:25
 * @description: 维护Connector与Transfer建立的连接
 */
@Singleton
public class ConnectorTransferContext {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorTransferContext.class);

    private List<ChannelHandlerContext> ctxList;
    /**
     * 全局范围内标识此台Connector的ConnectorId
     */
    private final String connectorId;

    public ConnectorTransferContext() {
        this.ctxList = new ArrayList<>();
        this.connectorId = IdWorker.UUID();
    }

    /**
     * 获取标识此台Connector的ConnectorId
     * @return
     */
    public String getConnectorId() {
        return this.connectorId;
    }

    /**
     * 获取一个连接到Transfer的ctx
     * @param time
     * @return
     */
    public ChannelHandlerContext getOneTransferCtx(long time) {
        if (ctxList.size() == 0) {
            logger.warn("Connector not connect to transfer.");
            return null;
        }
        return ctxList.get((int) (time % ctxList.size()));
    }

    /**
     * 添加一个transfer ctx
     */
    public synchronized void addTransferCtx(ChannelHandlerContext ctx) {
        this.ctxList.add(ctx);
    }

    /**
     * 删除失效的transfer ctx
     * @param ctx
     */
    public synchronized void removeTransferCtx(ChannelHandlerContext ctx) {
        this.ctxList.remove(ctx);
    }
}
