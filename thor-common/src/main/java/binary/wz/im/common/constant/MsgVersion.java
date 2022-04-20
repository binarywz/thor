package binary.wz.im.common.constant;

import java.util.stream.Stream;

/**
 * @author binarywz
 * @date 2022/4/19 23:42
 * @description:
 */
public enum MsgVersion {
    /**
     * version 1
     */
    V1(1);

    private int version;

    MsgVersion(int version) {
        this.version = version;
    }

    public static MsgVersion get(int version) {
        return Stream.of(values()).filter(item -> item.version == version)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getVersion() {
        return version;
    }
}
