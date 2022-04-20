package binary.wz.im.session.ack;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.session.processor.RcvMsgProcessor;
import binary.wz.im.session.util.IdWorker;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/4/19 23:33
 * @description: 接收方窗口: 1.不重复; 2.不乱序
 */
public class RcvAckWindow {
    private static Logger logger = LoggerFactory.getLogger(RcvAckWindow.class);

    private final int maxSize;
    private AtomicBoolean first;
    /**
     * 接收方在当前会话中收到的最后一条消息的ID，避免消息重复
     */
    private AtomicLong lastId;
    /**
     * 接收方消息暂存Map，处理乱序消息
     */
    private ConcurrentHashMap<Long, RcvMsgProcessor> notContinuousMap;

    public RcvAckWindow(int maxSize) {
        this.first = new AtomicBoolean(true);
        this.maxSize = maxSize;
        this.lastId = new AtomicLong(-1);
        this.notContinuousMap = new ConcurrentHashMap<>();
    }

    /**
     * 维护会话接收到的消息队列
     *
     * @param mid
     * @param from
     * @param dest
     * @param ctx
     * @param rcvMessage
     * @param rcvFunction
     * @return
     */
    public CompletableFuture<Void> offer(Long mid, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                                         ChannelHandlerContext ctx, Message rcvMessage, Consumer<Message> rcvFunction) {
        // 若消息重复，表明发送方没收到ACK，则发送ACK
        if (isRepeat(mid)) {
            ctx.writeAndFlush(getInternalAck(mid, from, dest));
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }
        RcvMsgProcessor rcvMsgProcessor = new RcvMsgProcessor(mid, from, dest, ctx, rcvMessage, rcvFunction);
        if (!isContinuous(mid)) {
            if (notContinuousMap.size() >= maxSize) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(new ImException("rcv window is full"));
                return future;
            }
            notContinuousMap.put(mid, rcvMsgProcessor);
            return rcvMsgProcessor.getFuture();
        }
        // 处理消息
        return processAsync(rcvMsgProcessor);
    }

    private CompletableFuture<Void> processAsync(RcvMsgProcessor processor) {
        return CompletableFuture
                .runAsync(processor::process)
                .thenAccept(ignore -> {
                    processor.sendAck();
                    processor.complete();
                })
                .thenAccept(ignore -> {
                    lastId.set(processor.getId());
                    notContinuousMap.remove(processor.getId());
                })
                .thenComposeAsync(ignore -> {
                    Long nextId = nextId(processor.getId());
                    if (notContinuousMap.containsKey(nextId)) {
                        // there is a next msg waiting in the map
                        RcvMsgProcessor nextProcessor = notContinuousMap.get(nextId);
                        return processAsync(nextProcessor);
                    } else {
                        // that's the newest msg
                        return processor.getFuture();
                    }
                })
                .exceptionally(e -> {
                    logger.error("[process received msg] has error", e);
                    return null;
                });
    }

    private Long nextId(Long mid) {
        return mid + 1;
    }

    /**
     * 判断消息是否重复
     *
     * @param mid
     * @return
     */
    private boolean isRepeat(Long mid) {
        return mid <= lastId.get();
    }

    /**
     * 判断消息是否连续
     *
     * @param mid
     * @return
     */
    private boolean isContinuous(Long mid) {
        // 如果是本次会话的第一条消息
        if (first.compareAndSet(true, false)) {
            return true;
        } else {
            // 不是第一条消息，按照公式计算
            return mid - lastId.get() == 1;
        }
    }

    private Internal.InternalMsg getInternalAck(Long mid, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest) {
        return Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.snowGenId())
                .setFrom(from)
                .setDest(dest)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(String.valueOf(mid))
                .build();
    }
}
