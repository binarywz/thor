package binary.wz.im.session.processor;

import binary.wz.im.common.function.ImBiConsumer;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author binarywz
 * @date 2022/4/26 0:06
 * @description: 主要处理INTERNAL/STATE不同TYPE消息的抽象处理器
 */
public abstract class AbstractMessageEnumProcessor<E extends ProtocolMessageEnum, M extends Message> {

    private Map<E, ImBiConsumer<M, ChannelHandlerContext>> processorMap;

    public AbstractMessageEnumProcessor(int size) {
        this.processorMap = new HashMap<>(size);
    }

    public void register(E type, ImBiConsumer<M, ChannelHandlerContext> consumer) {
        processorMap.put(type, consumer);
    }

    /**
     * 获取枚举
     * @param msg
     * @return
     */
    protected abstract E getType(M msg);

    public ImBiConsumer<M, ChannelHandlerContext> generateFun() {
        return (m, ctx) -> Optional.ofNullable(processorMap.get(getType(m)))
                .orElseThrow(() -> new IllegalArgumentException("Invalid msg enum " + m.toString()))
                .accept(m, ctx);
    }
}
