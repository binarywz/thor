package binary.wz.im.session.processor;

import binary.wz.im.common.constant.MsgVersion;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.session.util.IdWorker;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/4/19 23:34
 * @description: 消息(接收)处理器，接收到消息后响应ACK
 */
public class RcvMsgProcessor {

    private Long mid;
    private Internal.InternalMsg.Module from;
    private Internal.InternalMsg.Module dest;
    private ChannelHandlerContext ctx;

    private CompletableFuture<Void> future;
    private Message message;
    private Consumer<Message> consumer;

    public RcvMsgProcessor(Long mid, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                           ChannelHandlerContext ctx, Message message, Consumer<Message> consumer) {
        this.mid = mid;
        this.from = from;
        this.dest = dest;
        this.ctx = ctx;
        this.message = message;
        this.consumer = consumer;
        this.future = new CompletableFuture<>();
    }

    /**
     * 处理消息
     *
     * @return
     */
    public Void process() {
        consumer.accept(message);
        return null;
    }

    /**
     * 响应ACK
     */
    public void sendAck() {
        if (ctx.channel().isOpen()) {
            Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
                    .setVersion(MsgVersion.V1.getVersion())
                    .setId(IdWorker.snowGenId())
                    .setFrom(from)
                    .setDest(dest)
                    .setCreateTime(System.currentTimeMillis())
                    .setMsgType(Internal.InternalMsg.MsgType.ACK)
                    .setMsgBody(String.valueOf(mid))
                    .build();
            ctx.writeAndFlush(ack);
        }
    }

    public void complete() {
        this.future.complete(null);
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }

    public Long getId() {
        return mid;
    }

    public Internal.InternalMsg.Module getFrom() {
        return from;
    }

    public Internal.InternalMsg.Module getDest() {
        return dest;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
