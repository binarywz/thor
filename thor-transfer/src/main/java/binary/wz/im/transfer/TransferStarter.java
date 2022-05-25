package binary.wz.im.transfer;

import binary.wz.im.transfer.config.TransferConfig;
import binary.wz.im.transfer.remoting.TransferServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/4/25 0:11
 * @description:
 */
public class TransferStarter {
    private final static Logger logger = LoggerFactory.getLogger(TransferStarter.class);

    public static void main(String[] args) {
        try {
            TransferServer.start();
        } catch (Exception e) {
            logger.error("[transfer] start failed", e);;
        }
    }
}
