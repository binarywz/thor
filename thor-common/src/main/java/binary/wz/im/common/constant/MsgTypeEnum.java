package binary.wz.im.common.constant;

import binary.wz.im.common.proto.Ack;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;

import java.util.stream.Stream;

/**
 * @author binarywz
 * @date 2022/4/17 22:37
 * @description: 消息类型
 */
public enum MsgTypeEnum {

    /**
     * 聊天消息
     */
    CHAT(0, Chat.ChatMsg.class),

    /**
     * 内部消息，如GREET/真正的ACK
     */
    INTERNAL(1, Internal.InternalMsg.class),

    /**
     * ACK消息，如已读/已投递
     */
    ACK(2, Ack.AckMsg.class);

    int code;
    Class<?> clazz;

    MsgTypeEnum(int code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static MsgTypeEnum getByCode(int code) {
        return Stream.of(values()).filter(item -> item.code == code)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public static MsgTypeEnum getByClass(Class<?> clazz) {
        return Stream.of(values()).filter(item -> item.clazz == clazz)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getCode() {
        return code;
    }
}
