package tool.common.log;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j(topic = "o.syslog")
@Component
public class MessageLoggerImpl implements MessageLogger {

    private static final String MDC_MSGID = "o.syslog.msgid";

    /**
     * 
     */
    @Autowired
    private MessageSource messageSource;

    @Override
    public void log(String messageId) {
        log(messageId, (Object) null);
    }

    @Override
    public void log(String messageId, Object... bindParams) {
        log(messageId, null, bindParams);
    }

    @Override
    public void log(String messageId, Throwable cause) {
        log(messageId, cause, (Object) null);
    }

    @Override
    public void log(String messageId, Throwable cause, Object... bindParams) {
        String message = getMessage(messageId, bindParams);
        logging(messageId, message, cause);
    }

    /**
     * 
     * @param messageId
     * @param bindParams
     * @return
     */
    private String getMessage(String messageId, Object... bindParams) {
        return this.messageSource.getMessage(messageId, bindParams, LocaleContextHolder.getLocale());
    }

    /**
     * 
     * @param messageId
     * @param message
     * @param cause
     */
    private void logging(String messageId, String message, Throwable cause) {
        MDC.put(MDC_MSGID, messageId);
        try {
            String logLevel = StringUtils.substring(messageId, 0, 2);
            switch (logLevel) {
            case "E_":
                if (Objects.nonNull(cause)) {
                    log.error(message, cause);
                } else {
                    log.error(message);
                }
                break;
            case "W_":
                if (Objects.nonNull(cause)) {
                    log.warn(message, cause);
                } else {
                    log.warn(message);
                }
                break;
            case "I_":
                if (Objects.nonNull(cause)) {
                    log.info(message, cause);
                } else {
                    log.info(message);
                }
                break;
            case "D_":
                if (Objects.nonNull(cause)) {
                    log.debug(message, cause);
                } else {
                    log.debug(message);
                }
                break;
            case "T_":
                if (Objects.nonNull(cause)) {
                    log.trace(message, cause);
                } else {
                    log.trace(message);
                }
                break;
            default:
                log.warn("messageId [{}] has unknown log level.", messageId);
            }
        } finally {
            MDC.remove(MDC_MSGID);
        }
    }
}
