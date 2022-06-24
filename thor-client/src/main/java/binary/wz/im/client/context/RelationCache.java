package binary.wz.im.client.context;

import binary.wz.im.common.domain.po.RelationDetail;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/11 17:11
 * @description:
 */
public interface RelationCache {
    /**
     * add relation
     * @param relationDetail
     */
    void addRelation(RelationDetail relationDetail);

    /**
     * batch add relation
     * @param relations
     */
    void addRelations(List<RelationDetail> relations);

    /**
     * get relation by userId
     * @param userId1
     * @param userId2
     * @param token
     * @return
     */
    RelationDetail getRelation(String userId1, String userId2, String token);
}
