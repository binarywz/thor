package binary.wz.im.rest.spi;

import binary.wz.im.rest.domain.UserBase;

/**
 * @author binarywz
 * @date 2022/6/3 11:15
 * @description: 通过spi实现不同的账号体系
 */
public interface UserSpi<T extends UserBase> {
    /**
     * get user by username and password
     * @param username
     * @param pwd
     * @return
     */
    T getUser(String username, String pwd);

    /**
     * get user bu id
     * @param id
     * @return
     */
    T getUserById(String id);
}
