package binary.wz.im.client.handler.codec;

import binary.wz.im.client.context.UserContext;
import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.util.Encryption;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/11 22:59
 * @description:
 */
public class AesEncoder extends MessageToMessageEncoder<Message> {
    private final static Logger logger = LoggerFactory.getLogger(AesEncoder.class);

    private UserContext userContext;

    public AesEncoder(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        try {
            if (msg instanceof Chat.ChatMsg) {
                Chat.ChatMsg cm = (Chat.ChatMsg) msg;
                Relation relation = userContext.getRelation(cm.getFromId(), cm.getDestId());
                String[] keys = relation.getEncryptKey().split("\\|");

                byte[] encodeBody = Encryption.encrypt(keys[0], keys[1], cm.getMsgBody().toByteArray());

                Chat.ChatMsg encodeMsg = Chat.ChatMsg.newBuilder().mergeFrom(cm)
                        .setMsgBody(ByteString.copyFrom(encodeBody)).build();
                out.add(encodeMsg);
            } else {
                out.add(msg);
            }
        } catch (Exception e) {
            logger.error("AesEncoder#encode error", e);
        }
    }
}
