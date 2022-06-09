package binary.wz.im.rest.spi.impl;

import binary.wz.im.common.domain.po.User;
import binary.wz.im.rest.domain.UserBase;
import binary.wz.im.rest.service.UserService;
import binary.wz.im.rest.spi.UserSpi;
import org.springframework.stereotype.Service;

/**
 * @author binarywz
 * @date 2022/6/3 11:19
 * @description:
 */
@Service
public class DefaultUserSpiImpl implements UserSpi<UserBase> {

    private final UserService userService;

    public DefaultUserSpiImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserBase getUser(String username, String pwd) {
        User user = userService.verifyAndGet(username, pwd);
        if (user == null) {
            return null;
        }

        UserBase userBase = new UserBase();
        userBase.setId(String.valueOf(user.getId()));
        userBase.setUsername(user.getUsername());
        return userBase;
    }

    @Override
    public UserBase getUserById(String id) {
        User user = userService.getById(Long.parseLong(id));
        if (user == null) {
            return null;
        }

        UserBase userBase = new UserBase();
        userBase.setId(String.valueOf(user.getId()));
        userBase.setUsername(user.getUsername());
        return userBase;
    }
}
