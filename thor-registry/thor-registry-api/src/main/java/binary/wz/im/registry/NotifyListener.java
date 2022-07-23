package binary.wz.im.registry;

import binary.wz.im.registry.domain.ServiceConfig;

/**
 * @author binarywz
 * @date 2022/7/17 21:31
 * @description:
 */
public interface NotifyListener {
    /**
     * 当收到服务变更通知时触发
     */
    void notify(ServiceConfig config);
}
