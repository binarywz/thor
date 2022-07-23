package binary.wz.im.registry;

import binary.wz.im.registry.domain.ServiceConfig;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/7/12 23:51
 * @description:
 */
public interface RegistryService {
    /**
     * 注册服务
     * @param path
     * @param config
     */
    void register(String path, ServiceConfig config);

    /**
     * 取消注册
     * @param path
     */
    void unregister(String path);

    /**
     * 订阅
     * @param notifyListener
     */
    void subscribe(NotifyListener notifyListener);

    /**
     * 取消订阅
     * @param notifyListener
     */
    void unsubscribe();

    /**
     * 查询符合条件的已注册数据，与订阅的推模式相对应，这里为拉模式
     * @return
     */
    List<ServiceConfig> lookup();
}
