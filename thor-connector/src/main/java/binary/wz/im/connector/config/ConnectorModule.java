package binary.wz.im.connector.config;

import binary.wz.im.status.factory.UserStatusServiceFactory;
import binary.wz.im.status.service.UserStatusService;
import binary.wz.im.status.service.impl.RedisUserStatusServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author binarywz
 * @date 2022/4/28 23:51
 * @description:
 */
public class ConnectorModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(UserStatusService.class, RedisUserStatusServiceImpl.class)
                .build(UserStatusServiceFactory.class));
    }
}
