package binary.wz.im.common.domain;

import binary.wz.im.common.domain.po.RelationDetail;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/1 23:15
 * @description:
 */
public class UserInfo {

    private String id;

    private String username;

    private String token;

    private List<RelationDetail> relations;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<RelationDetail> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationDetail> relations) {
        this.relations = relations;
    }
}
