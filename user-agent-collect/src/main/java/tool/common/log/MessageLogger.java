package tool.common.log;

/**
 * 
 * @author kamiyama ryohei
 *
 */
public interface MessageLogger {

    /**
    * 
    * @param messageId
    */
    void log(String messageId);

    /**
     * 
     * @param messageId
     * @param bindParams
     */
    void log(String messageId, Object... bindParams);

    /**
     * 
     * @param messageId
     * @param cause
     */
    void log(String messageId, Throwable cause);

    /**
     * 
     * @param messageId
     * @param cause
     * @param bindParams
     */
    void log(String messageId, Throwable cause, Object... bindParams);
}
