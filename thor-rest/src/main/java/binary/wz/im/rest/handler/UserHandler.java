package binary.wz.im.rest.handler;

import binary.wz.im.common.domain.ResultWrapper;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.rest.domain.UserBase;
import binary.wz.im.rest.domain.vo.UserReq;
import binary.wz.im.rest.filter.TokenManager;
import binary.wz.im.rest.service.RelationService;
import binary.wz.im.rest.spi.SpiFactory;
import binary.wz.im.rest.spi.UserSpi;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author binarywz
 * @date 2022/6/3 17:46
 * @description:
 */
@Component
public class UserHandler {

    private UserSpi<? extends UserBase> userSpi;
    private RelationService relationService;
    private TokenManager tokenManager;

    public UserHandler(SpiFactory spiFactory, RelationService relationService, TokenManager tokenManager) {
        this.userSpi = spiFactory.getUserSpi();
        this.relationService = relationService;
        this.tokenManager = tokenManager;
    }

    /**
     * 登录
     * @param request
     * @return
     */
    public Mono<ServerResponse> login(ServerRequest request) {
        return ValidateHandler.validateBody(req ->
                        req.flatMap(login -> {
                            UserBase user = userSpi.getUser(login.getUsername(), login.getPwd());
                            return user != null ? Mono.just(user) : Mono.empty();
                        })
                                .flatMap(u -> tokenManager.createNewToken(u.getId())
                                        .map(t -> {
                                            UserInfo userInfo = new UserInfo();
                                            userInfo.setId(u.getId());
                                            userInfo.setUsername(u.getUsername());
                                            userInfo.setToken(t);
                                            return userInfo;
                                        }))
                                .flatMap(u -> Flux.fromIterable(relationService.getFriends(u.getId()))
                                        .collectList()
                                        .map(list -> {
                                            u.setRelations(list);
                                            return u;
                                        }))
                                .map(ResultWrapper::success)
                                .flatMap(info -> ok().body(fromObject(info)))
                                .switchIfEmpty(Mono.error(new ImException("[rest] authentication failed")))
                , request, UserReq.class);
    }

    /**
     * 注销
     * @param request
     * @return
     */
    public Mono<ServerResponse> logout(ServerRequest request) {
        String token = request.headers().header("token").get(0);

        return tokenManager.expire(token).map(ResultWrapper::wrapBool)
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(r)));
    }
}
