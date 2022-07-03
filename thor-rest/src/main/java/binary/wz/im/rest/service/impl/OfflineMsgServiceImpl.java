package binary.wz.im.rest.service.impl;

import binary.wz.im.common.constant.MsgType;
import binary.wz.im.common.domain.po.OfflineMsg;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.Notify;
import binary.wz.im.rest.mapper.OfflineMsgMapper;
import binary.wz.im.rest.service.OfflineMsgService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/2 23:30
 * @description:
 */
@Service
public class OfflineMsgServiceImpl extends ServiceImpl<OfflineMsgMapper, OfflineMsg> implements OfflineMsgService {

    @Override
    public void saveChat(Chat.ChatMsg msg) {
        OfflineMsg offlineMsg = new OfflineMsg();
        offlineMsg.setMsgId(msg.getId());
        offlineMsg.setMsgCode(MsgType.CHAT.getCode());
        offlineMsg.setToUserId(msg.getDestId());
        offlineMsg.setContent(msg.toByteArray());
        saveOfflineMsg(offlineMsg);
    }

    @Override
    public void saveNotify(Notify.NotifyMsg msg) {
        OfflineMsg offlineMsg = new OfflineMsg();
        offlineMsg.setMsgId(msg.getId());
        offlineMsg.setMsgCode(MsgType.NOTIFY.getCode());
        offlineMsg.setToUserId(msg.getDestId());
        offlineMsg.setContent(msg.toByteArray());
        save(offlineMsg);
    }

    private void saveOfflineMsg(OfflineMsg offlineMsg) {
        if (!save(offlineMsg)) {
            throw new ImException("[offline] save offline msg failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OfflineMsg> pollOfflineMsgList(String userId) {
        List<OfflineMsg> unreadList = list(new LambdaQueryWrapper<OfflineMsg>()
                .eq(OfflineMsg::getToUserId, userId)
                .eq(OfflineMsg::getHasRead, false)
                .orderByDesc(OfflineMsg::getMsgId));
        unreadList.parallelStream().forEach(msg -> baseMapper.markReadOfflineMsg(msg.getMsgId()));
        return unreadList;
    }
}
