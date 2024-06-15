package stub.common.online.filter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import stub.common.online.exception.OnlineServiceException;
import stub.common.online.filter.annotation.OnlineProcessEntry;

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
     * @param joinPoint
     * @param onlineProcessEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(onlineProcessEntry)")
    public Object intercept(ProceedingJoinPoint joinPoint, OnlineProcessEntry onlineProcessEntry) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (OnlineServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new OnlineServiceException(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
