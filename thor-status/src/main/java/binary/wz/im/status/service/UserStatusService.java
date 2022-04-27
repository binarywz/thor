package binary.wz.im.status.service;

/**
 * @author binarywz
 * @date 2022/4/24 23:15
 * @description:
 */
public interface UserStatusService {
    /**
     * user online
     * @param userId
     * @param connectorId
     * @return the user's previous connector id, if not exist then return null
     */
    String online(String userId, String connectorId);

    /**
     * user offline
     * @param userId
     */
    void offline(String userId);

    /**
     * get connector id by user id
     * @param userId
     * @return
     */
    String getConnectorId(String userId);
}
