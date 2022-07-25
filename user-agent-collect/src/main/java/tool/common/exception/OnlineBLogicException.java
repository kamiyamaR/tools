package tool.common.exception;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class OnlineBLogicException extends RuntimeException {
    private String messageId;
    private Object[] messageBindParams;
    private HttpStatus statusCode;
    private MultiValueMap<String, String> responseHeaders;
    private Object responseBody;

    /**
     * 
     * @param messageId
     * @param statusCode
     */
    public OnlineBLogicException(String messageId, HttpStatus statusCode) {
        this(messageId, statusCode, (Object) null);
    }

    /**
     * 
     * @param messageId
     * @param statusCode
     * @param messageBindParams
     */
    public OnlineBLogicException(String messageId, HttpStatus statusCode, Object... messageBindParams) {
        this(messageId, statusCode, null, null, messageBindParams);
    }

    /**
     * 
     * @param messageId
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineBLogicException(String messageId, HttpStatus statusCode, MultiValueMap<String, String> responseHeaders,
            Object responseBody) {
        this(messageId, statusCode, responseHeaders, responseBody, (Object) null);
    }

    /**
     * 
     * @param messageId
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     * @param messageBindParams
     */
    public OnlineBLogicException(String messageId, HttpStatus statusCode, MultiValueMap<String, String> responseHeaders,
            Object responseBody, Object... messageBindParams) {
        super();
        this.messageId = messageId;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        if (Objects.nonNull(messageBindParams)) {
            this.messageBindParams = messageBindParams;
        }
    }

    /**
     * 
     * @param messageId
     * @param cause
     * @param statusCode
     */
    public OnlineBLogicException(String messageId, Throwable cause, HttpStatus statusCode) {
        this(messageId, cause, statusCode, (Object) null);
    }

    /**
     * 
     * @param messageId
     * @param cause
     * @param statusCode
     * @param messageBindParams
     */
    public OnlineBLogicException(String messageId, Throwable cause, HttpStatus statusCode,
            Object... messageBindParams) {
        this(messageId, cause, statusCode, null, null, messageBindParams);
    }

    /**
     * 
     * @param messageId
     * @param cause
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineBLogicException(String messageId, Throwable cause, HttpStatus statusCode,
            MultiValueMap<String, String> responseHeaders, Object responseBody) {
        this(messageId, cause, statusCode, responseHeaders, responseBody, (Object) null);
    }

    /**
     * 
     * @param messageId
     * @param cause
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     * @param messageBindParams
     */
    public OnlineBLogicException(String messageId, Throwable cause, HttpStatus statusCode,
            MultiValueMap<String, String> responseHeaders, Object responseBody, Object... messageBindParams) {
        super(cause);
        this.messageId = messageId;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        if (Objects.nonNull(messageBindParams)) {
            this.messageBindParams = messageBindParams;
        }
    }
}
