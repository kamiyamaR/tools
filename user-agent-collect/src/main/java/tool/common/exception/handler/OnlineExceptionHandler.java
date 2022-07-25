package tool.common.exception.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = OnlineBLogicException.class)
    public ResponseEntity<Object> handleException(OnlineBLogicException ex) {
        this.messageLogger.log(ex.getMessageId(), ex.getCause(), ex.getMessageBindParams());
        return new ResponseEntity<Object>(ex.getResponseBody(), ex.getResponseHeaders(), ex.getStatusCode());
    }

    /**
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Void> handleException(Exception ex) {
        this.messageLogger.log("E_COM_FW_000_001", ex);
        return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
