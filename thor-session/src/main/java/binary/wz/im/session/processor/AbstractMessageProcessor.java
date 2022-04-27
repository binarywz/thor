package binary.wz.im.session.processor;

import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.function.ImBiConsumer;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author binarywz
 * @date 2022/4/25 23:27
 * @description: 抽象消息处理器
 */
public abstract class AbstractMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageProcessor.class);

    // 处理器容器: 根据不同消息类型使用不同的Consumer进行处理
    private Map<Class<? extends Message>, ImBiConsumer<? extends Message, ChannelHandlerContext>> processorMap;

    protected AbstractMessageProcessor() {
        this.processorMap = new HashMap<>();
        registerProcessors();
    }

    /**
     * 注册Message处理器
     */
    public abstract void registerProcessors();

    protected <T extends Message> void register(Class<T> clazz, ImBiConsumer<T, ChannelHandlerContext> consumer) {
        processorMap.put(clazz, consumer);
    }

    @SuppressWarnings("unchecked")
    public void process(Message msg, ChannelHandlerContext ctx) {
        ImBiConsumer consumer = processorMap.get(msg.getClass());
        if (consumer == null) {
            logger.warn("[message parser] unexpected msg: {}", msg.toString());
        }
        doProcess(msg, msg.getClass(), ctx, consumer);
    }

    private <T extends Message> void doProcess(Message msg, Class<T> clazz, ChannelHandlerContext ctx, ImBiConsumer<T, ChannelHandlerContext> consumer) {
        T m = clazz.cast(msg);
        try {
            consumer.accept(m, ctx);
        } catch (Exception e) {
            throw new ImException("[Message process] has error", e);
        }
    }
}
