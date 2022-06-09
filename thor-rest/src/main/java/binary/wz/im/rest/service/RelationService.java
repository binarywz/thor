package binary.wz.im.rest.service;

import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.domain.po.RelationDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/2 23:10
 * @description:
 */
public interface RelationService extends IService<Relation> {
    /**
     * 查询好友列表
     * @param userId
     * @return
     */
    List<RelationDetail> getFriends(String userId);

    /**
     *
     * @param userId1
     * @param userId2
     * @return
     */
    Long saveRelation(String userId1, String userId2);
}
