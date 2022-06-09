package binary.wz.im.rest.service;

import binary.wz.im.common.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author binarywz
 * @date 2022/6/2 23:07
 * @description:
 */
public interface UserService extends IService<User> {
    /**
     * 验证用户密码，成功则返回用户，失败返回null
     * @param username
     * @param pwd
     * @return
     */
    User verifyAndGet(String username, String pwd);
}
