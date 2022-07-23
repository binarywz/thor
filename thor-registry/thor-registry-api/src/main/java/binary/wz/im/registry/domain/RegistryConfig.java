package binary.wz.im.registry.domain;

/**
 * @author binarywz
 * @date 2022/7/17 21:28
 * @description:
 */
public class RegistryConfig {
    private String address;

    public RegistryConfig(String address) {
        this.address = address;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
