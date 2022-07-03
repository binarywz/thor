package binary.wz.im.rest.mapper;

import binary.wz.im.common.domain.po.OfflineMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author binarywz
 * @date 2022/6/1 23:57
 * @description:
 */
public interface OfflineMsgMapper extends BaseMapper<OfflineMsg> {
    /**
     * 标识读取离线消息
     * @param msgId
     * @return
     */
    int markReadOfflineMsg(String msgId);
}
