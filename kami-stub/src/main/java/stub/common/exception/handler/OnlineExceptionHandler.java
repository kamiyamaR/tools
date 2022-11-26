package stub.common.exception.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
     * ステータスコード 400.<br>
     * {@link BindException}でapplication/x-www-form-urlencodedのパラメータがパースできなかった場合などに発生.<br>
     * 
     * @param ex
     * @return
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { BindException.class })
    public Map<String, Object> handleBindException(BindException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }

    /**
     * ステータスコード 400.<br>
     * {@link RequestBody}でJSONパラメータがパースできなかった場合などに発生.<br>
     * パースで失敗する例<br>
     * ・int型の項目にString型の値が設定された.<br>
     * ・int型の項目に「2147483648」が設定された.<br>
     * 
     * @param ex
     * @return
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { HttpMessageNotReadableException.class })
    public Map<String, Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }

    /**
     * ステータスコード 400.<br>
     * {@link PathVariable}でURLパスパラメータがパースできなかった、<br>
     * もしくは、{@link RequestParam}でクエリパラメータがパースできなかった場合などに発生.<br>55555
     * パースで失敗する例<br>
     * ・int型の項目にString型の値が設定された.<br>
     * ・int型の項目に「2147483648」が設定された.<br>
     * 
     * @param ex
     * @return
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
    public Map<String, Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }

    /**
     * ステータスコード 400.<br>
     * GETで必須になっているクエリパラメータが指定されていない場合.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { MissingServletRequestParameterException.class })
    public Map<String, Object> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 400.<br>
     * HttpSessionに指定したオブジェクトが格納されていない場合.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { ServletRequestBindingException.class })
    public Map<String, Object> handleServletRequestBindingException(ServletRequestBindingException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 404.<br>
     * コンテキストパス誤りの場合に発生.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = { NoHandlerFoundException.class })
    public Map<String, Object> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 415.<br>
     * .<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(value = { HttpMediaTypeNotSupportedException.class })
    public Map<String, Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 405.<br>
     * URIは対応しているがMethodが対応していないものを指定された.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(value = { HttpRequestMethodNotSupportedException.class })
    public Map<String, Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * 
     * @param ex
     * @return
     */
    /*
    @ExceptionHandler(value = { Throwable.class })
    public ResponseEntity<Object> handleThrowable(Throwable ex) {
        log.error("Throwableをハンドルしました。", ex);
        return ResponseEntity.internalServerError().build();
    }
    */
}
