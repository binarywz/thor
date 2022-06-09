package binary.wz.im.common.domain;

/**
 * @author binarywz
 * @date 2022/6/1 23:17
 * @description:
 */
public class ResultWrapper<T> {

    private final static String SUCCESS = "success";

    private Integer code;
    private String msg;
    private T data;

    public ResultWrapper(){}

    public ResultWrapper(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResultWrapper<T> success() {
        ResultWrapper<T> resultWrapper = new ResultWrapper<>();
        resultWrapper.setCode(200);
        resultWrapper.setMsg(SUCCESS);

        return resultWrapper;
    }

    public static <T> ResultWrapper<T> success(T data) {
        ResultWrapper<T> resultWrapper = success();
        resultWrapper.setData(data);
        return resultWrapper;
    }


    public static ResultWrapper<String> fail(String message) {
        ResultWrapper<String> resultWrapper = new ResultWrapper<>();
        resultWrapper.setCode(500);
        resultWrapper.setMsg(message);
        return resultWrapper;
    }

    public static ResultWrapper<String> wrapBool(boolean success) {
        return success ? success() : fail("operation failed, please try again!");
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
