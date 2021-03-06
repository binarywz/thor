package binary.wz.im.session.ack;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/4/20 0:06
 * @description: 消息(处理器)，处理未收到ACK的消息
 */
public class SndMessageProcessor<T extends Message> {
    private static Logger logger = LoggerFactory.getLogger(SndMessageProcessor.class);

    private Message sndMessage;
    private Consumer<Message> sndFunction;
    private CompletableFuture<T> future;

    private volatile AtomicLong sndTime;
    private volatile AtomicBoolean sending;
    private volatile AtomicInteger times;

    public SndMessageProcessor(Message sndMessage, Consumer<Message> sndFunction) {
        this.sndMessage = sndMessage;
        this.sndFunction = sndFunction;
        this.future = new CompletableFuture<>();
        this.sndTime = new AtomicLong(0);
        this.sending = new AtomicBoolean(false);
        this.times = new AtomicInteger(0);
    }

    /**
     * 发送消息
     */
    public void send() {
        if (sending.compareAndSet(false, true)) {
            this.sndTime.set(System.nanoTime());
            try {
                sndFunction.accept(sndMessage);
            } catch (Exception e) {
                logger.error("send msg failed, msg:{}", sndMessage, e);
            } finally {
                this.sending.set(false);
            }
        }
    }

    public long timeElapse() {
        return System.nanoTime() - sndTime.get();
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public AtomicLong getSndTime() {
        return sndTime;
    }

    public AtomicBoolean getSending() {
        return sending;
    }
}
