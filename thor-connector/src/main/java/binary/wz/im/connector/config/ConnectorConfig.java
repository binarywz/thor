package binary.wz.im.connector.config;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/4/28 23:43
 * @description:
 */
public class ConnectorConfig {

    public final static Injector injector;
    public final static Integer port;
    public final static String[] transferUrls;
    public final static String redisHost;
    public final static Integer redisPort;
    public final static String redisPassword;

    public final static String registryAddress;
    public final static String serviceGroup;

    static {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("connector.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        port = Integer.parseInt(properties.getProperty("port"));
        transferUrls = (properties.getProperty("transfer.url")).split(",");
        redisHost = properties.getProperty("redis.host");
        redisPort = Integer.parseInt(properties.getProperty("redis.port"));
        redisPassword = properties.getProperty("redis.password");

        registryAddress = properties.getProperty("registry.address");
        serviceGroup = properties.getProperty("registry.connector.service.group");

        System.setProperty("log.path", properties.getProperty("log.path"));
        System.setProperty("log.level", properties.getProperty("log.level"));
        injector = Guice.createInjector(new ConnectorModule());
    }
}
