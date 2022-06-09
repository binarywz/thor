package binary.wz.im.rest.spi;

import binary.wz.im.common.exception.ImException;
import binary.wz.im.rest.domain.UserBase;
import binary.wz.im.rest.spi.impl.DefaultUserSpiImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author binarywz
 * @date 2022/6/3 11:16
 * @description:
 */
@Component
public class SpiFactory implements ApplicationContextAware {

    @Value("${spi.user.impl.class:}")
    private String userSpiImplClassName;

    private UserSpi<? extends UserBase> userSpi;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public UserSpi<? extends UserBase> getUserSpi() {
        if (StringUtils.isBlank(userSpiImplClassName)) {
            return applicationContext.getBean(DefaultUserSpiImpl.class);
        }
        try {
            if (userSpi == null) {
                Class<?> userSpiImplClass = Class.forName(userSpiImplClassName);
                userSpi = (UserSpi<? extends UserBase>) applicationContext.getBean(userSpiImplClass);
            }
            return userSpi;
        } catch (ClassNotFoundException e) {
            throw new ImException("can not find class: " + userSpiImplClassName);
        }
    }
}
