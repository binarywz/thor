package binary.wz.im.session.ack;

import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Internal;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author binarywz
 * @date 2022/4/19 0:16
 * @description: 发送方窗口，维护ACK等待队列
 */
public class SndAckWindow {
    private static Logger logger = LoggerFactory.getLogger(SndAckWindow.class);

    /**
     * 分两种情况:
     * 1.服务端发送窗口，windowMap中存在多组{conn:serverAckWindow}
     * 2.客户端发送窗口，windowMap中只有一组{conn:serverAckWindow}
     */
    private static Map<Serializable, SndAckWindow> windowMap;

    /**
     * 发送方ACK等待队列轮询线程
     */
    private static ExecutorService executorService;

    private final Duration timeout;
    private final int maxSize;

    /**
     * ACK等待队列
     * key: 消息Id
     * value: ack processor，处理未收到ack的消息
     */
    private ConcurrentHashMap<String, SndMessageProcessor<Internal.InternalMsg>> sndMsgProcessorMap;

    static {
        windowMap = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(SndAckWindow::checkTimeoutAndRetry);
    }

    public SndAckWindow(Serializable connectionId, int maxSize, Duration timeout) {
        this.sndMsgProcessorMap = new ConcurrentHashMap<>();
        this.timeout = timeout;
        this.maxSize = maxSize;
        windowMap.put(connectionId, this);
    }

    /**
     * 发送消息入队
     * TODO mid使用UUID代替
     * @param connectionId
     * @param mid
     * @param sndMessage
     * @param sndFunction
     * @return
     */
    public static CompletableFuture<Internal.InternalMsg> offer(Serializable connectionId, String mid, Message sndMessage, Consumer<Message> sndFunction) {
        return windowMap.get(connectionId).offer(mid, sndMessage, sndFunction);
    }

    /**
     * 发送消息入队
     * TODO mid使用UUID代替
     * @param mid
     * @param sndMessage
     * @param sndFunction
     * @return
     */
    public CompletableFuture<Internal.InternalMsg> offer(String mid, Message sndMessage, Consumer<Message> sndFunction) {
        if (sndMsgProcessorMap.containsKey(mid)) {
            CompletableFuture<Internal.InternalMsg> future = new CompletableFuture<>();
            future.completeExceptionally(new ImException("send repeat msg id: " + mid));
            return future;
        }
        if (sndMsgProcessorMap.size() > maxSize) {
            CompletableFuture<Internal.InternalMsg> future = new CompletableFuture<>();
            future.completeExceptionally(new ImException("snd window is full"));
            return future;
        }
        SndMessageProcessor<Internal.InternalMsg> sndMessageProcessor = new SndMessageProcessor<>(sndMessage, sndFunction);
        sndMessageProcessor.send();
        sndMsgProcessorMap.put(mid, sndMessageProcessor);
        return sndMessageProcessor.getFuture();
    }

    /**
     * 处理对应消息的ACK
     * @param message
     */
    public void ack(Internal.InternalMsg message) {
        String id = message.getMsgBody();
        logger.debug("get ack, msg: {}", id);
        if (sndMsgProcessorMap.containsKey(id)) {
            sndMsgProcessorMap.get(id).getFuture().complete(message);
            sndMsgProcessorMap.remove(id);
        }
    }

    /**
     * single thread do
     * 轮询处理ACK等待队列，若有超时未收到ACK的，就取出消息重发
     * 超时未收到ACK消息的两种处理方式:
     * 1.与TCP/IP一样不断发送直到收到ACK为止
     * 2.设置一个最大重试次数，超过这个次数还没收到ACK，就使用失败机制处理
     *
     * // TODO 此处采用了第一种方式，第二种方式更为合理
     */
    private static void checkTimeoutAndRetry() {
        while (true) {
            for (SndAckWindow window : windowMap.values()) {
                window.sndMsgProcessorMap.entrySet().stream()
                        .filter(entry -> window.timeout(entry.getValue()))
                        .forEach(entry -> window.retry(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void retry(String id, SndMessageProcessor<?> processor) {
        logger.warn("retry send msg: {}", id);
        processor.send();
    }

    private boolean timeout(SndMessageProcessor processor) {
        return processor.getSndTime().get() != 0 && processor.timeElapse() > timeout.toNanos();
    }
}
