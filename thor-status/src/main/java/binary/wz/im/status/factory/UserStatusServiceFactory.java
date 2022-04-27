package binary.wz.im.status.factory;

import binary.wz.im.status.service.UserStatusService;

import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/4/24 23:33
 * @description:
 */
public interface UserStatusServiceFactory {
    /**
     * create a userStatusService
     * @param properties
     * @return
     */
    UserStatusService createService(Properties properties);
}
