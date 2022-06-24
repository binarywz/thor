package binary.wz.im.client.domain.vo;

/**
 * @author binarywz
 * @date 2022/6/9 23:56
 * @description:
 */
public class UserReq {

    private String username;
    private String pwd;

    public UserReq(String username, String pwd) {
        this.username = username;
        this.pwd = pwd;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
