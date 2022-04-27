package binary.wz.im.transfer.config;

import binary.wz.im.status.factory.UserStatusServiceFactory;
import binary.wz.im.status.service.UserStatusService;
import binary.wz.im.status.service.impl.RedisUserStatusServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author binarywz
 * @date 2022/4/25 0:12
 * @description:
 */
public class TransferModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(UserStatusService.class, RedisUserStatusServiceImpl.class)
                .build(UserStatusServiceFactory.class));
    }
}
