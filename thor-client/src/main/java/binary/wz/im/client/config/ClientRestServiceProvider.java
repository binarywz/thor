package binary.wz.im.client.config;

import binary.wz.im.client.service.ClientRestService;
import com.google.inject.Provider;

/**
 * @author binarywz
 * @date 2022/6/12 20:48
 * @description:
 */
public class ClientRestServiceProvider implements Provider<ClientRestService> {
    @Override
    public ClientRestService get() {
        return new ClientRestService(ClientConfig.restUrl);
    }
}
