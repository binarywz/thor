package binary.wz.im.client.config;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author binarywz
 * @date 2022/6/9 23:59
 * @description:
 */
public class ClientConfig {

    public final static String connectorHost;
    public final static Integer connectorPort;
    public final static String restUrl;
    public final static Injector injector;

    static {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("client.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectorHost = (properties.getProperty("connector.host"));
        connectorPort = Integer.parseInt(properties.getProperty("connector.port"));
        restUrl = properties.getProperty("rest.url");
        injector = Guice.createInjector(new ClientModule());
    }
}
