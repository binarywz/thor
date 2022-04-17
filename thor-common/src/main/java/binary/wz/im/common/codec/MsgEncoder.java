package binary.wz.im.common.codec;

import binary.wz.im.common.constant.MsgTypeEnum;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/4/17 23:08
 * @description:
 */
public class MsgEncoder extends MessageToByteEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        try {
            byte[] bytes = msg.toByteArray();
            int code = MsgTypeEnum.getByClass(msg.getClass()).getCode();
            int len = bytes.length;

            ByteBuf buf = Unpooled.buffer(8 + len);
            buf.writeInt(len);
            buf.writeInt(code);
            buf.writeBytes(bytes);
            out.writeBytes(buf);
        } catch (Exception e) {
            logger.error("[IM MsgEncoder] msg encode has error", e);
        }

    }
}
