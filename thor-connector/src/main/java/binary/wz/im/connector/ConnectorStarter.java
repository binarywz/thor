package binary.wz.im.connector;

import binary.wz.im.connector.remoting.ConnectorClient;
import binary.wz.im.connector.remoting.ConnectorServer;

/**
 * @author binarywz
 * @date 2022/4/28 23:53
 * @description:
 */
public class ConnectorStarter {
    public static void main(String[] args) {
        new ConnectorClient().start();
        ConnectorServer.start();
    }
}
