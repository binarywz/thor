package binary.wz.im.client.config;

import binary.wz.im.client.context.RelationCache;
import binary.wz.im.client.context.impl.MemoryRelationCache;
import binary.wz.im.client.service.ClientRestService;
import com.google.inject.AbstractModule;

/**
 * @author binarywz
 * @date 2022/6/9 23:59
 * @description:
 */
public class ClientModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RelationCache.class).to(MemoryRelationCache.class);
        bind(ClientRestService.class).toProvider(ClientRestServiceProvider.class);
    }
}
