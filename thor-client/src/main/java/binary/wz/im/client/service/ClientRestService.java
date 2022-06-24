package binary.wz.im.client.service;

import binary.wz.im.client.domain.vo.UserReq;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.domain.po.RelationDetail;
import binary.wz.im.common.rest.AbstractRestService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/11 18:00
 * @description:
 */
public class ClientRestService extends AbstractRestService<RestService> {

    @Inject
    public ClientRestService(@Assisted String url) {
        super(RestService.class, url);
    }

    public UserInfo login(String username, String pwd) {
        return doRequest(() -> restClient.login(new UserReq(username, pwd)).execute());
    }

    public Void logout(String token) {
        return doRequest(() -> restClient.logout(token).execute());
    }

    public List<RelationDetail> friends(String userId, String token) {
        return doRequest(() -> restClient.friends(userId, token).execute());
    }

    public RelationDetail relation(String userId1, String userId2, String token) {
        return doRequest(() -> restClient.relation(userId1, userId2, token).execute());
    }
}
