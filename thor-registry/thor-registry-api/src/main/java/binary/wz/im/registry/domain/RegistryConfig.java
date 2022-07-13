package binary.wz.im.registry.domain;

/**
 * @author binarywz
 * @date 2022/7/12 23:59
 * @description:
 */
public class RegistryConfig {
    private String registryAddress;
    private String host;
    private Integer port;
    private String group;   // 标识不同分组，可用于区分不同机房
    private String token;   // 握手token
    private Integer status; // 服务状态，1在线，0下线

    public RegistryConfig(String registryAddress, String host, Integer port, String group, String token, Integer status) {
        this.registryAddress = registryAddress;
        this.host = host;
        this.port = port;
        this.group = group;
        this.token = token;
        this.status = status;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
