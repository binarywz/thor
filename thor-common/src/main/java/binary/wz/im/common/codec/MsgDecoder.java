package binary.wz.im.common.codec;

import binary.wz.im.common.parser.MessageParser;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/4/17 23:00
 * @description:
 */
public class MsgDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MsgDecoder.class);

    private MessageParser messageParser;

    public MsgDecoder() {
        this.messageParser = new MessageParser();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();

        if (in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }

        // 解析消息体长度
        int len = in.readInt();
        if (len < 0) {
            ctx.close();
            logger.error("[IM MsgDecoder] message length less than 0, channel closed");
            return;
        }
        if (len > in.readableBytes() - 4) {
            in.resetReaderIndex();
            return;
        }

        int code = in.readInt();
        ByteBuf byteBuf = Unpooled.buffer(len);
        in.readBytes(byteBuf);
        byte[] body = byteBuf.array();
        Message m = messageParser.parseMessage(code, body);
        out.add(m);
    }
}
