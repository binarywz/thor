package binary.wz.im.session.ack;

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
public class RcvMessageProcessor {

    private String id; // 消息全局唯一标识
    private Long seq;  // 消息系列号
    private String fromId; // 发送者
    private String destId; // 接收者
    private ChannelHandlerContext ctx;

    private CompletableFuture<Void> future;
    private Message message;
    private Consumer<Message> consumer;

    public RcvMessageProcessor(String id, Long seq, String fromId, String destId,
                               ChannelHandlerContext ctx, Message message, Consumer<Message> consumer) {
        this.id = id;
        this.seq = seq;
        this.fromId = fromId;
        this.destId = destId;
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
    public void process() {
        consumer.accept(message);
    }

    public void complete() {
        this.future.complete(null);
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }

    public String getId() {
        return id;
    }

    public String getFromId() {
        return fromId;
    }

    public String getDestId() {
        return destId;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
