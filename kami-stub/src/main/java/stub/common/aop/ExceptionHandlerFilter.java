package stub.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import stub.common.aop.annotation.OnlineProcessEntry;
import stub.common.exception.OnlineServiceException;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Aspect
@Component
@Order(value = 1)
public class ExceptionHandlerFilter {

    /**
     * 
     * @param point
     * @param onlineProcessEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(onlineProcessEntry)")
    public Object intercept(ProceedingJoinPoint point, OnlineProcessEntry onlineProcessEntry) throws Throwable {
        try {
            return point.proceed();
        } catch (OnlineServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new OnlineServiceException(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
