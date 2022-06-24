package binary.wz.im.rest.handler;

import binary.wz.im.common.domain.ResultWrapper;
import binary.wz.im.common.domain.po.Relation;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.rest.domain.vo.RelationReq;
import binary.wz.im.rest.service.RelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * @author binarywz
 * @date 2022/6/3 17:35
 * @description:
 */
@Component
public class RelationHandler {

    private RelationService relationService;

    public RelationHandler(RelationService relationService) {
        this.relationService = relationService;
    }

    /**
     * 查询好友列表
     * @param request
     * @return
     */
    public Mono<ServerResponse> getFriends(ServerRequest request) {
        String userId = request.pathVariable("id");

        return Flux.fromIterable(relationService.getFriends(userId))
                .collectList()
                .map(ResultWrapper::success)
                .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }

    /**
     * 查询userId1/userId2之间的relation
     * @param request
     * @return
     */
    public Mono<ServerResponse> getRelation(ServerRequest request) {
        String user1 = request.queryParam("userId1").orElseThrow(() -> new ImException("param userId1 can not be null"));
        String user2 = request.queryParam("userId2").orElseThrow(() -> new ImException("param userId2 can not be null"));

        Long userId1 = Long.parseLong(user1);
        Long userId2 = Long.parseLong(user2);

        Mono<Relation> relationMono = Mono.fromCallable(() -> relationService.getOne(new LambdaQueryWrapper<Relation>()
                .eq(Relation::getUserId1, Math.min(userId1, userId2))
                .eq(Relation::getUserId2, Math.max(userId1, userId2))));

        return relationMono.map(ResultWrapper::success)
                .flatMap(r -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(r)))
                .switchIfEmpty(Mono.error(new ImException("no relation")));
    }

    /**
     * 保存relation
     * @param request
     * @return
     */
    public Mono<ServerResponse> saveRelation(ServerRequest request) {
        return ValidateHandler.validateBody(req -> req.map(
                r -> relationService.saveRelation(r.getUserId1(), r.getUserId2()))
                .map(relationId -> ImmutableMap.of(relationId, String.valueOf(relationId)))
                .map(ResultWrapper::success)
                .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res))),
                request, RelationReq.class);
    }

    /**
     * 删除relation
     * @param request
     * @return
     */
    public Mono<ServerResponse> deleteRelation(ServerRequest request) {
        String relationId = request.pathVariable("id");
        return request.bodyToMono(RelationReq.class)
                .flatMap(r -> Mono.fromCallable(() -> relationService.removeById(relationId)))
                .map(ResultWrapper::wrapBool)
                .defaultIfEmpty(ResultWrapper.success())
                .flatMap(res -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }
}
