package stub.common.online.filter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import stub.common.online.filter.annotation.OnlineProcessEntry;
import stub.common.tool.Stopwatch;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Aspect
@Component
@Order(value = 2)
public class OnlineProcessFunctionFilter {

    /**
     * 
     * @param joinPoint
     * @param onlineProcessEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(onlineProcessEntry)")
    public Object intercept(ProceedingJoinPoint joinPoint, OnlineProcessEntry onlineProcessEntry) throws Throwable {
        String target = new StringBuilder().append(joinPoint.getTarget().getClass().getName()).append('.')
                .append(joinPoint.getSignature().getName()).toString();
        Stopwatch stopwatch = new Stopwatch().start();
        try {
            log.info("------ {} ------- START.", target);
            Object result = joinPoint.proceed();
            log.info("------ {} ------- END. [{}]", target, stopwatch);
            return result;
        } catch (Exception e) {
            log.info("------ {} ------- ABEND. [{}]", target, stopwatch);
            throw e;
        }
    }

}
