package stub.common.exception.handler;

import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import stub.common.exception.OnlineServiceException;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@RestControllerAdvice
public class OnlineExceptionHandler {

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { OnlineServiceException.class })
    public ResponseEntity<Object> handleOnlineServiceException(OnlineServiceException ex) {
        log.error("", ex);

        BodyBuilder builder = ResponseEntity.status(ex.getStatusCode());

        if (Objects.nonNull(ex.getResponseHeaders())) {
            HttpHeaders headers = new HttpHeaders(ex.getResponseHeaders());
            builder.headers(headers);
        }

        if (Objects.nonNull(ex.getResponseBody())) {
            return builder.body(ex.getResponseBody());
        } else {
            return builder.build();
        }
    }

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { Throwable.class })
    public ResponseEntity<Object> handleException(Throwable ex) {
        log.error("Throwableをハンドルしました。", ex);
        return ResponseEntity.internalServerError().build();
    }
}
