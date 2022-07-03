package binary.wz.im.session.ack;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.session.util.IdWorker;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/4/19 23:33
 * @description: 接收方窗口: 1.不重复; 2.不乱序
 */
public class RcvAckWindow {
    private static Logger logger = LoggerFactory.getLogger(RcvAckWindow.class);

    private final int maxSize;
    // private AtomicBoolean first;
    /**
     * 接收方在当前会话中收到的最后一条消息的ID，避免消息重复
     * TODO 后续无需维护lastId，按收到的消息时间排序
     */
    // private AtomicLong lastId;
    /**
     * 接收方消息暂存Map，处理乱序消息
     * TODO 后续不处理乱序消息，仅收到消息后回复ACK
     */
    private Set<String> rcvMessageSet;

    public RcvAckWindow(int maxSize) {
        // this.first = new AtomicBoolean(true);
        this.maxSize = maxSize;
        // this.lastId = new AtomicLong(-1);
        this.rcvMessageSet = new ConcurrentSkipListSet<>();
    }

    /**
     * 维护会话接收到的消息队列
     * @param mid
     * @param seq
     * @param fromId
     * @param destId
     * @param ctx
     * @param rcvMessage
     * @param rcvFunction
     * @return
     */
    public CompletableFuture<Void> offer(String mid, Long seq, String fromId, String destId,
                                         ChannelHandlerContext ctx, Message rcvMessage, Consumer<Message> rcvFunction) {
        // 若消息重复，表明发送方没收到ACK，则发送ACK
        if (isRepeat(mid)) {
            ctx.writeAndFlush(buildInternalAck(mid, seq, fromId, destId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }
        RcvMessageProcessor rcvMessageProcessor = new RcvMessageProcessor(mid, seq, fromId, destId, ctx, rcvMessage, rcvFunction);
        rcvMessageSet.add(mid);
        // 处理消息
        return processAsync(rcvMessageProcessor);
    }

    private CompletableFuture<Void> processAsync(RcvMessageProcessor processor) {
        return CompletableFuture
                .runAsync(processor::process)
                .exceptionally(e -> {
                    logger.error("[process received msg] has error", e);
                    return null;
                });
    }

    /**
     * 判断消息是否重复
     * @param mid
     * @return
     */
    private boolean isRepeat(String mid) {
        return rcvMessageSet.contains(mid);
    }

    private Internal.InternalMsg buildInternalAck(String mid, Long seq, String fromId, String destId) {
        return Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.UUID())
                .setSeq(seq) // seq是收到的消息的序列号，ack消息没有序列号
                .setFromId(fromId)
                .setDestId(destId)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(mid)
                .build();
    }
}
