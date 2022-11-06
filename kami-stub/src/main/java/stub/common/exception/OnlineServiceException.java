package stub.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import lombok.Getter;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Getter
public class OnlineServiceException extends RuntimeException {
    /** . */
    private final HttpStatus statusCode;
    /** . */
    private MultiValueMap<String, String> responseHeaders;
    /** . */
    private Object responseBody;

    /**
     * 
     * @param statusCode
     */
    public OnlineServiceException(final HttpStatus statusCode) {
        this(statusCode, null, null);
    }

    /**
     * 
     * @param statusCode
     * @param responseHeaders
     */
    public OnlineServiceException(final HttpStatus statusCode, MultiValueMap<String, String> responseHeaders) {
        this(statusCode, responseHeaders, null);
    }

    /**
     * 
     * @param statusCode
     * @param responseBody
     */
    public OnlineServiceException(final HttpStatus statusCode, Object responseBody) {
        this(statusCode, null, responseBody);
    }

    /**
     * 
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineServiceException(final HttpStatus statusCode, MultiValueMap<String, String> responseHeaders,
            Object responseBody) {
        super();
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * 
     * @param cause
     * @param statusCode
     */
    public OnlineServiceException(Throwable cause, final HttpStatus statusCode) {
        this(cause, statusCode, null, null);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseHeaders
     */
    public OnlineServiceException(Throwable cause, final HttpStatus statusCode,
            MultiValueMap<String, String> responseHeaders) {
        this(cause, statusCode, responseHeaders, null);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseBody
     */
    public OnlineServiceException(Throwable cause, final HttpStatus statusCode, Object responseBody) {
        this(cause, statusCode, null, responseBody);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineServiceException(Throwable cause, final HttpStatus statusCode,
            MultiValueMap<String, String> responseHeaders, Object responseBody) {
        super(cause);
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }
}
