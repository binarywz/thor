package binary.wz.im.transfer.config;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/4/24 23:45
 * @description: Transfer配置
 */
public class TransferConfig {

    public final static Injector injector;
    public final static Integer port;
    public final static String redisHost;
    public final static Integer redisPort;
    public final static String redisPassword;

    public final static String registryAddress;
    public final static String serviceGroup;
    public final static Integer servicePort;
    public final static String serviceToken;

    static {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("transfer.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        port = Integer.parseInt(properties.getProperty("port"));
        redisHost = properties.getProperty("redis.host");
        redisPort = Integer.parseInt(properties.getProperty("redis.port"));
        redisPassword = properties.getProperty("redis.password");

        registryAddress = properties.getProperty("registry.address");
        servicePort = Integer.parseInt(properties.getProperty("registry.transfer.service.port"));
        serviceGroup = properties.getProperty("registry.transfer.service.group");
        serviceToken = properties.getProperty("registry.transfer.service.token");


        System.setProperty("log.path", properties.getProperty("log.path"));
        System.setProperty("log.level", properties.getProperty("log.level"));
        injector = Guice.createInjector(new TransferModule());
    }
}
