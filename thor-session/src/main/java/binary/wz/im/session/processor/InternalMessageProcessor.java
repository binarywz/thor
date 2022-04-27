package binary.wz.im.session.processor;

import binary.wz.im.common.proto.Internal;

/**
 * @author binarywz
 * @date 2022/4/26 23:35
 * @description: INTERNAL消息，如ACK消息
 */
public class InternalMessageProcessor extends AbstractMessageEnumProcessor<Internal.InternalMsg.MsgType, Internal.InternalMsg> {

    public InternalMessageProcessor(int size) {
        super(size);
    }

    @Override
    protected Internal.InternalMsg.MsgType getType(Internal.InternalMsg msg) {
        return msg.getMsgType();
    }
}
