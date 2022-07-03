package binary.wz.im.session.processor;

import binary.wz.im.common.proto.Notify;
import binary.wz.im.session.processor.AbstractMessageEnumProcessor;

/**
 * @author binarywz
 * @date 2022/4/26 23:30
 * @description: 通知消息，如已读/已投递消息，还有系统推送消息
 */
public class NotifyMessageProcessor extends AbstractMessageEnumProcessor<Notify.NotifyMsg.MsgType, Notify.NotifyMsg> {

    public NotifyMessageProcessor(int size) {
        super(size);
    }

    @Override
    protected Notify.NotifyMsg.MsgType getType(Notify.NotifyMsg msg) {
        return msg.getMsgType();
    }
}
