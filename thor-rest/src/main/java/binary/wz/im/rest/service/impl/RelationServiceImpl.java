package binary.wz.im.rest.service.impl;

import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.domain.po.RelationDetail;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.rest.domain.UserBase;
import binary.wz.im.rest.mapper.RelationMapper;
import binary.wz.im.rest.service.RelationService;
import binary.wz.im.rest.spi.SpiFactory;
import binary.wz.im.rest.spi.UserSpi;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/3 11:05
 * @description:
 */
@Service
public class RelationServiceImpl extends ServiceImpl<RelationMapper, Relation> implements RelationService {

    private UserSpi<? extends UserBase> userSpi;

    public RelationServiceImpl(SpiFactory spiFactory) {
        this.userSpi = spiFactory.getUserSpi();
    }

    @Override
    public List<RelationDetail> getFriends(String userId) {
        return baseMapper.getRelationList(userId);
    }

    @Override
    public Long saveRelation(String userId1, String userId2) {
        if (userId1.equals(userId2)) {
            throw new ImException("[rest] userId1 and userId2 can not be same");
        }
        if (userSpi.getUserById(userId1) == null || userSpi.getUserById(userId2) == null) {
            throw new ImException("[rest] user not exist");
        }
        String first = userId1.compareTo(userId2) >= 0 ? userId1 : userId2;
        String second = first.equals(userId1) ? userId2 : userId1;

        Relation relation = new Relation();
        relation.setUserId1(first);
        relation.setUserId2(second);
        relation.setEncryptKey(RandomStringUtils.randomAlphabetic(16) + "|" + RandomStringUtils.randomNumeric(16));

        if (save(relation)) {
            return relation.getId();
        } else {
            throw new ImException("[rest] save relation failed");
        }
    }
}
