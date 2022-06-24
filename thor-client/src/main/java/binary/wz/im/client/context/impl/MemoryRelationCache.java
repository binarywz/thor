package binary.wz.im.client.context.impl;

import binary.wz.im.client.context.RelationCache;
import binary.wz.im.client.service.ClientRestService;
import binary.wz.im.common.domain.po.RelationDetail;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author binarywz
 * @date 2022/6/11 17:27
 * @description:
 */
@Singleton
public class MemoryRelationCache implements RelationCache {

    private ConcurrentHashMap<String, RelationDetail> relationMap;
    private ClientRestService clientRestService;

    @Inject
    public MemoryRelationCache(ClientRestService clientRestService) {
        this.clientRestService = clientRestService;
        this.relationMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addRelation(RelationDetail relationDetail) {
        relationMap.put(buildKey(relationDetail.getUserId1(), relationDetail.getUserId2()), relationDetail);
    }

    @Override
    public void addRelations(List<RelationDetail> relations) {
        relationMap.putAll(relations.stream().collect(Collectors.toMap(
                r -> buildKey(r.getUserId1(), r.getUserId2()),
                r -> r)
        ));
    }

    @Override
    public RelationDetail getRelation(String userId1, String userId2, String token) {
        RelationDetail relationDetail = relationMap.get(buildKey(userId1, userId2));
        if (relationDetail == null) {
            relationDetail = getRelationFromRest(userId1, userId2, token);
            if (relationDetail != null) {
                relationMap.put(buildKey(userId1, userId2), relationDetail);
            }
        }
        return relationDetail;
    }

    private RelationDetail getRelationFromRest(String userId, String userId2, String token) {
        return clientRestService.relation(userId, userId2, token);
    }

    private String buildKey(String userId1, String userId2) {
        String max = userId1.compareTo(userId2) >= 0 ? userId1 : userId2;
        String min = max.equals(userId1) ? userId2 : userId1;
        return min + "_" + max;
    }
}
