package binary.wz.im.common.constant;

import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import binary.wz.im.common.proto.State;

import java.util.stream.Stream;

/**
 * @author binarywz
 * @date 2022/4/17 22:37
 * @description: 消息类型
 */
public enum MsgType {

    /**
     * 聊天消息
     */
    CHAT(0, Chat.ChatMsg.class),

    /**
     * 内部消息，如GREET/真正的ACK
     */
    INTERNAL(1, Internal.InternalMsg.class),

    /**
     * STATE消息，如已读/已投递
     */
    STATE(2, State.StateMsg.class);

    int code;
    Class<?> clazz;

    MsgType(int code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static MsgType getByCode(int code) {
        return Stream.of(values()).filter(item -> item.code == code)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public static MsgType getByClass(Class<?> clazz) {
        return Stream.of(values()).filter(item -> item.clazz == clazz)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getCode() {
        return code;
    }
}
