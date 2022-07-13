package binary.wz.im.registry;

import binary.wz.im.registry.domain.RegistryConfig;

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
    void register(String path, RegistryConfig config);

    /**
     * 取消注册
     * @param path
     */
    void unregister(String path);
}
