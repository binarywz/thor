package binary.wz.im.client.handler.codec;

import binary.wz.im.client.context.UserContext;
import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.util.Encryption;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/11 17:05
 * @description:
 */
public class AesDecoder extends MessageToMessageDecoder<Message> {
    private final static Logger logger = LoggerFactory.getLogger(AesDecoder.class);

    private UserContext userContext;

    public AesDecoder(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        try {
            if (msg instanceof Chat.ChatMsg) {
                Chat.ChatMsg cm = (Chat.ChatMsg) msg;
                Relation relation = userContext.getRelation(cm.getFromId(), cm.getDestId());
                String[] keys = relation.getEncryptKey().split("\\|");

                byte[] decodeBody = Encryption.decrypt(keys[0], keys[1], cm.getMsgBody().toByteArray());

                Chat.ChatMsg decodeMsg = Chat.ChatMsg.newBuilder().mergeFrom(cm)
                        .setMsgBody(ByteString.copyFrom(decodeBody)).build();
                out.add(decodeMsg);
            } else {
                out.add(msg);
            }
        } catch (Exception e) {
            logger.error("AesDecoder#decode error", e);
        }
    }
}
