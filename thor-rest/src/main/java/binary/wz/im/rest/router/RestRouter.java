package binary.wz.im.rest.router;

import binary.wz.im.rest.handler.OfflineMsgHandler;
import binary.wz.im.rest.handler.RelationHandler;
import binary.wz.im.rest.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * @author binarywz
 * @date 2022/6/3 23:05
 * @description:
 */
@Configuration
public class RestRouter {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler userHandler) {
        return RouterFunctions
                .route(POST("/user/login").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                        userHandler::login)
                .andRoute(GET("/user/logout").and(accept(APPLICATION_JSON)),
                        userHandler::logout);
    }

    @Bean
    public RouterFunction<ServerResponse> relationRoutes(RelationHandler relationHandler) {
        return RouterFunctions
                .route(GET("/relation/{id}").and(accept(APPLICATION_JSON)),
                        relationHandler::getFriends)
                .andRoute(GET("/relation").and(accept(APPLICATION_JSON)),
                        relationHandler::getRelation)
                .andRoute(POST("/relation").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)),
                        relationHandler::saveRelation)
                .andRoute(DELETE("/relation/{id}").and(accept(APPLICATION_JSON)),
                        relationHandler::deleteRelation);
    }

    /**
     * 拉取离线消息
     * TODO Client Or Connector poll
     * @param offlineMsgHandler
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> offlineMsgRoutes(OfflineMsgHandler offlineMsgHandler) {
        return RouterFunctions
                .route(GET("/offline/poll/{id}").and(accept(APPLICATION_JSON)),
                        offlineMsgHandler::pollOfflineMsg);
    }
}
