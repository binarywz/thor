package binary.wz.im.common.parser;

import binary.wz.im.common.constant.MsgType;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Ack;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * @author binarywz
 * @date 2022/4/17 22:48
 * @description: 解析CHAT/ACK/INTERNAL三种Protobuf Message
 */
public class MessageParser {

    private Map<MsgType, Parser> parserMap;

    public MessageParser() {
        this.parserMap = new HashMap<>(MsgType.values().length);
        this.parserMap.put(MsgType.CHAT, Chat.ChatMsg::parseFrom);
        this.parserMap.put(MsgType.ACK, Ack.AckMsg::parseFrom);
        this.parserMap.put(MsgType.INTERNAL, Internal.InternalMsg::parseFrom);
    }

    public Message parseMessage(int code, byte[] bytes) throws InvalidProtocolBufferException {
        MsgType msgType = MsgType.getByCode(code);
        Parser parser = parserMap.get(msgType);
        if (parser == null) {
            throw new ImException("[MessageParser], no proper parse function, msgType: " + msgType.name());
        }
        return parser.process(bytes);
    }

    @FunctionalInterface
    public interface Parser {
        /**
         * 解析protobuf message
         *
         * @param bytes
         * @return
         * @throws InvalidProtocolBufferException
         */
        Message process(byte[] bytes) throws InvalidProtocolBufferException;
    }
}
