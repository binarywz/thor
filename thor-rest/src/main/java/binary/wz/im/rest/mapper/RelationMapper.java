package binary.wz.im.rest.mapper;

import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.domain.po.RelationDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/1 23:57
 * @description:
 */
public interface RelationMapper extends BaseMapper<Relation> {
    /**
     * 查询好友列表
     * @return
     */
    List<RelationDetail> getRelationList(@Param("userId") String userId);
}
