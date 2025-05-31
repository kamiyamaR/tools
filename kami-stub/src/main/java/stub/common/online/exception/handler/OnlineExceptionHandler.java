package stub.common.online.exception.handler;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import stub.common.online.exception.OnlineServiceException;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@RestControllerAdvice
public class OnlineExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = { OnlineServiceException.class })
    public ResponseEntity<Object> handleOnlineServiceException(OnlineServiceException ex, WebRequest request) {
        log.error("", ex);

        request.getHeaderNames().forEachRemaining(
                headerName -> log.info(" {}:{}", headerName, Arrays.toString(request.getHeaderValues(headerName))));

        request.getParameterMap()
                .forEach((paramName, paramValues) -> log.info(" {}:{}", paramName, Arrays.toString(paramValues)));

        return new ResponseEntity<Object>(ex.getResponseBody(), ex.getResponseHeaders(), ex.getStatusCode());
    }

    /**
     * ステータスコード 400.<br>
     * {@link RequestParam}でapplication/x-www-form-urlencodedのパラメータがパースできなかった場合などに発生.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { BindException.class })
    public Map<String, Object> handleBindException(BindException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

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
    /*
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { HttpMessageNotReadableException.class })
    public Map<String, Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 400.<br>
     * {@link PathVariable}でURLパスパラメータがパースできなかった、<br>
     * もしくは、{@link RequestParam}でクエリパラメータがパースできなかった場合などに発生.<br>
     * パースで失敗する例<br>
     * ・int型の項目にString型の値が設定された.<br>
     * ・int型の項目に「2147483648」が設定された.<br>
     * 
     * @param ex
     * @return
     */
    /*
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
    public Map<String, Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("", ex);
        return new HashMap<String, Object>();
    }
    */

    /**
     * ステータスコード 400.<br>
     * 必須になっているクエリパラメータの指定が無かった場合.<br>
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
