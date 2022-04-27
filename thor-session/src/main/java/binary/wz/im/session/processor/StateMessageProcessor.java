package binary.wz.im.session.processor;

import binary.wz.im.common.proto.State;

/**
 * @author binarywz
 * @date 2022/4/26 23:30
 * @description: STATE消息，如已读/已投递消息
 */
public class StateMessageProcessor extends AbstractMessageEnumProcessor<State.StateMsg.MsgType, State.StateMsg> {

    public StateMessageProcessor(int size) {
        super(size);
    }

    @Override
    protected State.StateMsg.MsgType getType(State.StateMsg msg) {
        return msg.getMsgType();
    }
}
