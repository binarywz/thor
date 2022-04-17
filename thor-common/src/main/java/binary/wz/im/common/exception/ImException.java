package binary.wz.im.common.exception;

/**
 * @author binarywz
 * @date 2022/4/17 22:58
 * @description:
 */
public class ImException extends RuntimeException {

    public ImException(String message, Throwable e) {
        super(message, e);
    }

    public ImException(Throwable e) {
        super(e);
    }

    public ImException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
