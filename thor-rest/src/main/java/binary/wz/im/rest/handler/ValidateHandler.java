package binary.wz.im.rest.handler;

import binary.wz.im.common.exception.ImException;
import com.google.common.collect.Iterables;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Function;

/**
 * @author binarywz
 * @date 2022/6/1 23:46
 * @description:
 */
public class ValidateHandler {

    private static Validator validator;

    public static <T> Mono<ServerResponse> validateBody(Function<Mono<T>, Mono<ServerResponse>> function,
                                                        ServerRequest request, Class<T> bodyClazz) {
        return request
                .bodyToMono(bodyClazz)
                .flatMap(body -> {
                    Set<ConstraintViolation<T>> msg = validator.validate(body);
                    if (msg.isEmpty()) {
                        return function.apply(Mono.just(body));
                    } else {
                        ConstraintViolation cv = Iterables.get(msg, 0);
                        return Mono.error(new ImException(cv.getPropertyPath() + " " + cv.getMessage()));
                    }
                });
    }

    public static void setValidator(Validator validator) {
        ValidateHandler.validator = validator;
    }
}
