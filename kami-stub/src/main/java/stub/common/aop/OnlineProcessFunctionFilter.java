package stub.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import stub.common.aop.annotation.OnlineProcessEntry;
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
     * @param point
     * @param onlineProcessEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(onlineProcessEntry)")
    public Object intercept(ProceedingJoinPoint point, OnlineProcessEntry onlineProcessEntry) throws Throwable {
        String classPath = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        String target = new StringBuilder().append(classPath).append('.').append(methodName).toString();
        Stopwatch stopwatch = new Stopwatch().start();
        try {
            log.info("------ {} ------- START.", target);
            Object result = point.proceed();
            log.info("------ {} ------- END. [{}]", target, stopwatch);
            return result;
        } catch (Exception e) {
            log.info("------ {} ------- ABEND. [{}]", target, stopwatch);
            throw e;
        }
    }

}
