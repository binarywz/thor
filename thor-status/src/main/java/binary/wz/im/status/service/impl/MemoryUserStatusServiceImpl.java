package binary.wz.im.status.service.impl;

import binary.wz.im.status.service.UserStatusService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author binarywz
 * @date 2022/4/24 23:18
 * @description: it's for the test
 */
public class MemoryUserStatusServiceImpl implements UserStatusService {

    private ConcurrentHashMap<String, String> userConnectorMap;

    public MemoryUserStatusServiceImpl() {
        this.userConnectorMap = new ConcurrentHashMap<>();
    }

    @Override
    public String online(String userId, String connectorId) {
        return userConnectorMap.put(userId, connectorId);
    }

    @Override
    public void offline(String userId) {
        userConnectorMap.remove(userId);
    }

    @Override
    public String getConnectorId(String userId) {
        return userConnectorMap.get(userId);
    }

}
