package stub.common.online.exception;

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
    private final int statusCode;
    /** . */
    private MultiValueMap<String, String> responseHeaders;
    /** . */
    private Object responseBody;

    /**
     * 
     * @param statusCode
     */
    public OnlineServiceException(final int statusCode) {
        this(statusCode, null, null);
    }

    /**
     * 
     * @param statusCode
     * @param responseHeaders
     */
    public OnlineServiceException(final int statusCode, MultiValueMap<String, String> responseHeaders) {
        this(statusCode, responseHeaders, null);
    }

    /**
     * 
     * @param statusCode
     * @param responseBody
     */
    public OnlineServiceException(final int statusCode, Object responseBody) {
        this(statusCode, null, responseBody);
    }

    /**
     * 
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineServiceException(final int statusCode, MultiValueMap<String, String> responseHeaders,
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
    public OnlineServiceException(Throwable cause, final int statusCode) {
        this(cause, statusCode, null, null);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseHeaders
     */
    public OnlineServiceException(Throwable cause, final int statusCode,
            MultiValueMap<String, String> responseHeaders) {
        this(cause, statusCode, responseHeaders, null);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseBody
     */
    public OnlineServiceException(Throwable cause, final int statusCode, Object responseBody) {
        this(cause, statusCode, null, responseBody);
    }

    /**
     * 
     * @param cause
     * @param statusCode
     * @param responseHeaders
     * @param responseBody
     */
    public OnlineServiceException(Throwable cause, final int statusCode, MultiValueMap<String, String> responseHeaders,
            Object responseBody) {
        super(cause);
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

}
