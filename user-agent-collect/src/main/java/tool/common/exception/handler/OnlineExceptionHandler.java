package tool.common.exception.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tool.common.exception.OnlineBLogicException;
import tool.common.log.MessageLogger;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@RestControllerAdvice
public class OnlineExceptionHandler {

    @Autowired
    private MessageLogger messageLogger;

    @ExceptionHandler(value = MissingRequestHeaderException.class)
    public ResponseEntity<Void> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
    }

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = OnlineBLogicException.class)
    public ResponseEntity<Object> handleException(OnlineBLogicException ex) {
        this.messageLogger.log(ex.getMessageId(), ex.getCause(), ex.getMessageBindParams());
        HttpHeaders responseHeaders = ex.getResponseHeaders() != null ? new HttpHeaders(ex.getResponseHeaders()) : null;
        return new ResponseEntity<Object>(ex.getResponseBody(), responseHeaders, ex.getStatusCode());
    }

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Void> handleException(Exception ex) {
        this.messageLogger.log("E_COM_0001", ex);
        return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
