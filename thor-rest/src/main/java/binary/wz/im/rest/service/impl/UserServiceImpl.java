package binary.wz.im.rest.service.impl;

import binary.wz.im.common.domain.po.User;
import binary.wz.im.rest.mapper.UserMapper;
import binary.wz.im.rest.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

/**
 * @author binarywz
 * @date 2022/6/2 23:25
 * @description:
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User verifyAndGet(String username, String pwd) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return user != null ? verifyPassword(pwd, user.getSalt(), user.getPwdHash()) ? user : null : null;
    }

    private boolean verifyPassword(String pwd, String salt, String pwdHash) {
        String hashRes = DigestUtils.sha256Hex(pwd + salt);
        return hashRes.equals(pwdHash);
    }
}
