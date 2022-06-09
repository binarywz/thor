package binary.wz.im.rest.handler;

import binary.wz.im.common.domain.ResultWrapper;
import binary.wz.im.common.exception.ImException;
import binary.wz.im.rest.service.OfflineMsgService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author binarywz
 * @date 2022/6/3 17:27
 * @description:
 */
@Component
public class OfflineMsgHandler {

    private OfflineMsgService offlineMsgService;

    public OfflineMsgHandler(OfflineMsgService offlineMsgService) {
        this.offlineMsgService = offlineMsgService;
    }

    /**
     * 拉取离线消息
     * @param request
     * @return
     */
    public Mono<ServerResponse> pollOfflineMsg(ServerRequest request) {
        String userId = request.pathVariable("id");

        return Mono.fromSupplier(() -> {
            try {
                return offlineMsgService.pollOfflineMsgList(userId);
            } catch (Exception e) {
                throw new ImException(e);
            }
        }).map(ResultWrapper::success)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(res)));
    }
}
