package binary.wz.im.registry.domain;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2022/7/12 23:59
 * @description:
 */
public class ServiceConfig implements Serializable {
    private static final long serialVersionUID = 2666500517033395666L;
    private String host;
    private Integer port;
    private String group;   // 标识不同分组，可用于区分不同机房
    private String token;   // 握手token
    private Integer status; // 服务状态，1在线，0下线
    private Integer type;   // 服务类型，1:transfer，2:connector

    public ServiceConfig() {

    }

    public ServiceConfig(String host, Integer port, String group, String token, Integer status, Integer type) {
        this.host = host;
        this.port = port;
        this.group = group;
        this.token = token;
        this.status = status;
        this.type = type;
    }

    public ServiceConfig(String group, Integer type) {
        this.group = group;
        this.type = type;
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

    public String getKey() {
        return this.host + ":" + this.port;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("host:%s, port:%s, group:%s, status:%d", this.host, this.port, this.group, this.status);
    }
}
