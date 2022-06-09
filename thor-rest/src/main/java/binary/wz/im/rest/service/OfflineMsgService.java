package binary.wz.im.rest.service;

import binary.wz.im.common.domain.po.OfflineMsg;
import binary.wz.im.common.proto.Chat;
import binary.wz.im.common.proto.State;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/2 23:18
 * @description:
 */
public interface OfflineMsgService extends IService<OfflineMsg> {
    /**
     * save offline chat msg
     * @param msg
     */
    void saveChat(Chat.ChatMsg msg);

    /**
     * save offline state msg
     * @param msg
     */
    void saveState(State.StateMsg msg);

    /**
     * get user's offline msg
     * @param userId
     * @return
     */
    List<OfflineMsg> pollOfflineMsgList(String userId);
}
