package binary.wz.im.common.domain.po;

/**
 * @author binarywz
 * @date 2022/6/1 23:09
 * @description:
 */
public class User extends DbModel {

    private String username;

    private String pwdHash;

    private String salt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
