package binary.wz.im.session.util;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author binarywz
 * @date 2022/4/19 23:47
 * @description:
 */
public class IdWorker {

    private static SnowFlake snowFlake;

    static {
        snowFlake = new SnowFlake(1, 1);
    }

    public static String UUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * snowFlake
     * for InternalMsg
     *
     * @return
     */
    public static Long snowGenId() {
        return snowFlake.nextId();
    }
}
